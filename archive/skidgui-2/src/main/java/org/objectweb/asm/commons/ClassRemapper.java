package org.objectweb.asm.commons;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

/**
 * A {@link ClassVisitor} for type remapping.
 * 
 * @author Eugene Kuleshov
 */
public class ClassRemapper extends ClassVisitor {

	protected final Remapper remapper;

	protected String className;

	public ClassRemapper(final ClassVisitor cv, final Remapper remapper) {
		this(Opcodes.ASM5, cv, remapper);
	}

	protected ClassRemapper(final int api, final ClassVisitor cv, final Remapper remapper) {
		super(api, cv);
		this.remapper = remapper;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.className = name;
		super.visit(version, access, remapper.mapType(name), remapper.mapSignature(signature, false), remapper.mapType(superName), interfaces == null ? null : remapper.mapTypes(interfaces));
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		AnnotationVisitor av = super.visitAnnotation(remapper.mapDesc(desc), visible);
		return av == null ? null : createAnnotationRemapper(av);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		AnnotationVisitor av = super.visitTypeAnnotation(typeRef, typePath, remapper.mapDesc(desc), visible);
		return av == null ? null : createAnnotationRemapper(av);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		FieldVisitor fv = super.visitField(access, remapper.mapFieldName(className, name, desc), remapper.mapDesc(desc), remapper.mapSignature(signature, true), remapper.mapValue(value));
		return fv == null ? null : createFieldRemapper(fv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		String newDesc = remapper.mapMethodDesc(desc);
		MethodVisitor mv = super.visitMethod(access, remapper.mapMethodName(className, name, desc), newDesc, remapper.mapSignature(signature, false), exceptions == null ? null : remapper.mapTypes(exceptions));
		return mv == null ? null : createMethodRemapper(mv);
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		// TODO should innerName be changed?
		super.visitInnerClass(remapper.mapType(name), outerName == null ? null : remapper.mapType(outerName), innerName, access);
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		super.visitOuterClass(remapper.mapType(owner), name == null ? null : remapper.mapMethodName(owner, name, desc), desc == null ? null : remapper.mapMethodDesc(desc));
	}

	protected FieldVisitor createFieldRemapper(FieldVisitor fv) {
		return new FieldRemapper(fv, remapper);
	}

	protected MethodVisitor createMethodRemapper(MethodVisitor mv) {
		return new MethodRemapper(mv, remapper);
	}

	protected AnnotationVisitor createAnnotationRemapper(AnnotationVisitor av) {
		return new AnnotationRemapper(av, remapper);
	}
}
