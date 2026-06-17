package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.LanguageFeature;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.creator.AnnotationInterfaceCreator;
import io.smallrye.jdeparser.creator.ClassCreator;
import io.smallrye.jdeparser.creator.EnumCreator;
import io.smallrye.jdeparser.creator.InterfaceCreator;
import io.smallrye.jdeparser.creator.RecordCreator;
import io.smallrye.jdeparser.creator.SourceFileCreator;

/**
 * Implementation of {@link SourceFileCreator} that collects source file
 * contents (imports, type declarations, comments) and writes the complete file.
 * <p>
 * Writes the form: {@code package pkg; [imports] [type declarations]}.
 * <p>
 * Performs import resolution following JLS §6.5.2 precedence:
 * explicit single-type-imports (step 1) shadow same-package types (step 3)
 * which shadow {@code java.lang} on-demand imports (step 4).
 */
public final class SourceFileCreatorImpl extends AbstractCreator implements SourceFileCreator, Writable {

    /** The owning sources collection. */
    private final SourcesImpl sources;

    /** The package name. */
    private final String packageName;

    /** The file name (without .java). */
    private final String fileName;

    /** Collected import declarations (qualified names). */
    private final Set<String> imports = new LinkedHashSet<>();

    /** Collected static import declarations. */
    private final List<String> staticImports = new ArrayList<>();

    /** Collected module import declarations. */
    private final List<String> moduleImports = new ArrayList<>();

    /** Qualified names of all types used in this source file's content. */
    private final Set<String> usedQualifiedTypes = new LinkedHashSet<>();

    /** The file content (type declarations, comments, blank lines). */
    private final List<Writable> content = new ArrayList<>();

    /**
     * Constructs a new source file creator.
     *
     * @param sources the owning sources collection
     * @param version the source version
     * @param packageName the package name
     * @param fileName the file name (without extension)
     */
    public SourceFileCreatorImpl(final SourcesImpl sources, final SourceVersion version,
            final String packageName, final String fileName) {
        super(version);
        this.sources = sources;
        this.packageName = packageName;
        this.fileName = fileName;
        sourceFile(this);
    }

    /**
     * Returns the package name.
     *
     * @return the package name
     */
    public String packageName() {
        return packageName;
    }

    /**
     * Returns the file name.
     *
     * @return the file name
     */
    public String fileName() {
        return fileName;
    }

    /**
     * Registers a type as used within this source file, recursively
     * extracting qualified names from composite types.
     *
     * @param type the type to register
     */
    @Override
    protected void registerUsedType(final Type type) {
        if (type instanceof ReferenceType ref) {
            usedQualifiedTypes.add(ref.qualifiedName());
        } else if (type instanceof NarrowedType nt) {
            registerUsedType(nt.rawType());
            for (Type arg : nt.typeArgs()) {
                registerUsedType(arg);
            }
        } else if (type instanceof ArrayType at) {
            registerUsedType(at.elementType());
        } else if (type instanceof WildcardType wt) {
            registerUsedType(wt.bound());
        } else if (type instanceof NestedType nt) {
            registerUsedType(nt.outer());
        } else if (type instanceof IntersectionType it) {
            for (Type t : it.types()) {
                registerUsedType(t);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void import_(final Type type) {
        checkNotDone();
        Assert.checkNotNullParam("type", type);
        if (type.erasure() instanceof ReferenceType ref) {
            imports.add(ref.qualifiedName());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void import_(final Class<?> clazz) {
        checkNotDone();
        Assert.checkNotNullParam("clazz", clazz);
        imports.add(clazz.getCanonicalName());
    }

    /** {@inheritDoc} */
    @Override
    public void importStatic(final Type type, final String member) {
        checkNotDone();
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("member", member);
        Assert.checkNotEmptyParam("member", member);
        if (type.erasure() instanceof ReferenceType ref) {
            staticImports.add(ref.qualifiedName() + "." + member);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void importModule(final String moduleName) {
        checkNotDone();
        Assert.checkNotNullParam("moduleName", moduleName);
        Assert.checkNotEmptyParam("moduleName", moduleName);
        version().require(LanguageFeature.MODULE_IMPORTS);
        moduleImports.add(moduleName);
    }

    /** {@inheritDoc} */
    @Override
    public void class_(final String name, final Consumer<ClassCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final ClassCreatorImpl cc = new ClassCreatorImpl(version(), name, true);
        cc.sourceFile(this);
        nest(() -> builder.accept(cc));
        cc.finish();
        content.add(cc);
    }

    /** {@inheritDoc} */
    @Override
    public void enum_(final String name, final Consumer<EnumCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final EnumCreatorImpl ec = new EnumCreatorImpl(version(), name);
        ec.sourceFile(this);
        nest(() -> builder.accept(ec));
        ec.finish();
        content.add(ec);
    }

    /** {@inheritDoc} */
    @Override
    public void interface_(final String name, final Consumer<InterfaceCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final InterfaceCreatorImpl ic = new InterfaceCreatorImpl(version(), name);
        ic.sourceFile(this);
        nest(() -> builder.accept(ic));
        ic.finish();
        content.add(ic);
    }

    /** {@inheritDoc} */
    @Override
    public void record_(final String name, final Consumer<RecordCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        version().require(LanguageFeature.RECORDS);
        final RecordCreatorImpl rc = new RecordCreatorImpl(version(), name);
        rc.sourceFile(this);
        nest(() -> builder.accept(rc));
        rc.finish();
        content.add(rc);
    }

    /** {@inheritDoc} */
    @Override
    public void annotationInterface_(final String name, final Consumer<AnnotationInterfaceCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final AnnotationInterfaceCreatorImpl ac = new AnnotationInterfaceCreatorImpl(version(), name);
        ac.sourceFile(this);
        nest(() -> builder.accept(ac));
        ac.finish();
        content.add(ac);
    }

    /** {@inheritDoc} */
    @Override
    public void blankLine() {
        checkActive();
        content.add(SourceFileWriter::nl);
    }

    /** {@inheritDoc} */
    @Override
    public void lineComment(final String comment) {
        checkActive();
        Assert.checkNotNullParam("comment", comment);
        content.add(w -> {
            w.write(Tokens.$COMMENT_TOK.LINE);
            w.sp();
            w.writeUnescaped(comment);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void blockComment(final String comment) {
        checkActive();
        Assert.checkNotNullParam("comment", comment);
        content.add(w -> {
            w.write(Tokens.$COMMENT_TOK.OPEN);
            w.sp();
            w.writeUnescaped(comment);
            w.sp();
            w.write(Tokens.$COMMENT_TOK.CLOSE);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // build the set of same-package simple names from used types and defined types
        final Set<String> samePackageSimpleNames = new LinkedHashSet<>();
        for (String qn : usedQualifiedTypes) {
            int dot = qn.lastIndexOf('.');
            if (dot >= 0 && qn.substring(0, dot).equals(packageName)) {
                samePackageSimpleNames.add(qn.substring(dot + 1));
            }
        }
        samePackageSimpleNames.addAll(sources.getDefinedSimpleNames(packageName));

        // build a map from import simple name → qualified name
        final Map<String, String> importsBySimpleName = new LinkedHashMap<>();
        for (String imp : imports) {
            int dot = imp.lastIndexOf('.');
            if (dot >= 0) {
                importsBySimpleName.put(imp.substring(dot + 1), imp);
            }
        }

        // configure the writer's class name resolver (JLS §6.5.2 precedence)
        writer.setClassNameResolver(qualifiedName -> {
            int lastDot = qualifiedName.lastIndexOf('.');
            if (lastDot < 0) {
                return qualifiedName;
            }
            String typePkg = qualifiedName.substring(0, lastDot);
            String simpleName = qualifiedName.substring(lastDot + 1);

            // step 1: explicit single-type-import
            if (imports.contains(qualifiedName)) {
                return simpleName;
            }
            // shadowed by a different explicit import with the same simple name?
            String importedQN = importsBySimpleName.get(simpleName);
            if (importedQN != null && !importedQN.equals(qualifiedName)) {
                return qualifiedName;
            }
            // step 3: same-package type
            if (typePkg.equals(packageName)) {
                return simpleName;
            }
            // step 4: java.lang type (unless shadowed by same-package)
            if ("java.lang".equals(typePkg)) {
                if (samePackageSimpleNames.contains(simpleName)) {
                    return qualifiedName;
                }
                return simpleName;
            }
            return qualifiedName;
        });

        // package declaration
        if (!packageName.isEmpty()) {
            writer.write(Tokens.$KW.PACKAGE);
            writer.writeName(packageName);
            writer.write(Tokens.$PUNCT.SEMI);
            writer.nl();
            writer.nl();
        }
        // imports (filtering out java.lang and same-package)
        boolean hasImports = false;
        for (String imp : imports) {
            int dot = imp.lastIndexOf('.');
            if (dot >= 0) {
                String impPkg = imp.substring(0, dot);
                if ("java.lang".equals(impPkg) || impPkg.equals(packageName)) {
                    continue;
                }
            }
            writer.write(Tokens.$KW.IMPORT);
            writer.writeName(imp);
            writer.write(Tokens.$PUNCT.SEMI);
            writer.nl();
            hasImports = true;
        }
        for (String imp : staticImports) {
            writer.write(Tokens.$KW.IMPORT);
            writer.write(Tokens.$KW.STATIC);
            writer.writeName(imp);
            writer.write(Tokens.$PUNCT.SEMI);
            writer.nl();
            hasImports = true;
        }
        for (String mod : moduleImports) {
            writer.write(Tokens.$KW.IMPORT);
            writer.write(Tokens.$KW.MODULE);
            writer.writeName(mod);
            writer.write(Tokens.$PUNCT.SEMI);
            writer.nl();
            hasImports = true;
        }
        if (hasImports) {
            writer.nl();
        }
        // type declarations and other content
        boolean firstContent = true;
        for (Writable item : content) {
            if (!firstContent) {
                writer.nl();
            }
            firstContent = false;
            item.write(writer);
            writer.nl();
        }
    }
}
