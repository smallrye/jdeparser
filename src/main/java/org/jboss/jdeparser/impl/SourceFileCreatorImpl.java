package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.LanguageFeature;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.AnnotationInterfaceCreator;
import org.jboss.jdeparser.creator.ClassCreator;
import org.jboss.jdeparser.creator.EnumCreator;
import org.jboss.jdeparser.creator.InterfaceCreator;
import org.jboss.jdeparser.creator.ModifierLocation;
import org.jboss.jdeparser.creator.RecordCreator;
import org.jboss.jdeparser.creator.SourceFileCreator;

/**
 * Implementation of {@link SourceFileCreator} that collects source file
 * contents (imports, type declarations, comments) and writes the complete file.
 * <p>
 * Writes the form: {@code package pkg; [imports] [type declarations]}.
 */
public final class SourceFileCreatorImpl extends AbstractCreator implements SourceFileCreator, Writable {

    /** The package name. */
    private final String packageName;

    /** The file name (without .java). */
    private final String fileName;

    /** Collected import declarations. */
    private final List<String> imports = new ArrayList<>();

    /** Collected static import declarations. */
    private final List<String> staticImports = new ArrayList<>();

    /** Collected module import declarations. */
    private final List<String> moduleImports = new ArrayList<>();

    /** The file content (type declarations, comments, blank lines). */
    private final List<Writable> content = new ArrayList<>();

    /**
     * Constructs a new source file creator.
     *
     * @param version     the source version
     * @param packageName the package name
     * @param fileName    the file name (without extension)
     */
    public SourceFileCreatorImpl(final SourceVersion version, final String packageName, final String fileName) {
        super(version);
        this.packageName = packageName;
        this.fileName = fileName;
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

    /** {@inheritDoc} */
    @Override
    public void import_(final JType type) {
        checkActive();
        if (type instanceof ReferenceJType ref) {
            imports.add(ref.qualifiedName());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void import_(final Class<?> clazz) {
        checkActive();
        imports.add(clazz.getCanonicalName());
    }

    /** {@inheritDoc} */
    @Override
    public void importStatic(final JType type, final String member) {
        checkActive();
        if (type instanceof ReferenceJType ref) {
            staticImports.add(ref.qualifiedName() + "." + member);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void importModule(final String moduleName) {
        checkActive();
        version().require(LanguageFeature.MODULE_IMPORTS);
        moduleImports.add(moduleName);
    }

    /** {@inheritDoc} */
    @Override
    public void class_(final String name, final Consumer<ClassCreator> builder) {
        checkActive();
        final ClassCreatorImpl cc = new ClassCreatorImpl(version(), name, true);
        nest(() -> builder.accept(cc));
        cc.finish();
        content.add(cc);
    }

    /** {@inheritDoc} */
    @Override
    public void enum_(final String name, final Consumer<EnumCreator> builder) {
        checkActive();
        final EnumCreatorImpl ec = new EnumCreatorImpl(version(), name);
        nest(() -> builder.accept(ec));
        ec.finish();
        content.add(ec);
    }

    /** {@inheritDoc} */
    @Override
    public void interface_(final String name, final Consumer<InterfaceCreator> builder) {
        checkActive();
        final InterfaceCreatorImpl ic = new InterfaceCreatorImpl(version(), name);
        nest(() -> builder.accept(ic));
        ic.finish();
        content.add(ic);
    }

    /** {@inheritDoc} */
    @Override
    public void record_(final String name, final Consumer<RecordCreator> builder) {
        checkActive();
        version().require(LanguageFeature.RECORDS);
        final RecordCreatorImpl rc = new RecordCreatorImpl(version(), name);
        nest(() -> builder.accept(rc));
        rc.finish();
        content.add(rc);
    }

    /** {@inheritDoc} */
    @Override
    public void annotationInterface_(final String name, final Consumer<AnnotationInterfaceCreator> builder) {
        checkActive();
        final AnnotationInterfaceCreatorImpl ac = new AnnotationInterfaceCreatorImpl(version(), name);
        nest(() -> builder.accept(ac));
        ac.finish();
        content.add(ac);
    }

    /** {@inheritDoc} */
    @Override
    public void blankLine() {
        checkActive();
        content.add(w -> w.nl());
    }

    /** {@inheritDoc} */
    @Override
    public void lineComment(final String comment) {
        checkActive();
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
        // package declaration
        if (!packageName.isEmpty()) {
            writer.write(Tokens.$KW.PACKAGE);
            writer.writeName(packageName);
            writer.write(Tokens.$PUNCT.SEMI);
            writer.nl();
            writer.nl();
        }
        // imports
        boolean hasImports = false;
        for (String imp : imports) {
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
