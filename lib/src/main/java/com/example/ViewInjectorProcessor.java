package com.example;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by lps on 2017/8/28.
 *
 * @version 1
 * @see
 * @since 2017/8/28 17:21
 */

@SupportedAnnotationTypes("com.example.InjectView")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ViewInjectorProcessor extends AbstractProcessor {
    private static final String GEN_CLASS_SUFFIX = "$Injector";
    private static final String INJECTOR_NAME = "ViewInjector";
    private static final String TAG = "ViewInjectorProcessor";
    private Types mTypeUtils;
    private Elements mElementUtils;
    private Filer mFiler;
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mTypeUtils = processingEnv.getTypeUtils();
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        使用了@InjectView注解的元素
        Set<? extends Element> mElementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(InjectView.class);
        if (mElementsAnnotatedWith.size() == 0) return true;
        Map<Element, List<Element>> elemtMap = new HashMap<>();
        StringBuffer buffer = new StringBuffer();
//        构建注解类
        buffer.append("package com.example;\n")
                .append("public class " + INJECTOR_NAME + "{\n");
        for (Element mElement : mElementsAnnotatedWith) {
            if (!isView(mElement.asType())) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, "is not a View ", mElement);
            }
            Element clazz = mElement.getEnclosingElement();
            addElment(elemtMap, clazz, mElement);
        }
        System.out.println(TAG+elemtMap);
//        遍历elemtmap，即遍历所有的注解类
        for (Map.Entry<Element, List<Element>> entry : elemtMap.entrySet()) {
            Element clazz = entry.getKey();
            String classname = clazz.getSimpleName().toString();
            String packageName = mElementUtils.getPackageOf(clazz).asType().toString();
            generateInjectorCode(packageName, classname, entry.getValue());
            String fullName = clazz.asType().toString();
//            拼接ViewInjector的inject方法代码
            buffer.append("\tpublic static void inject(" + fullName + " arg){\n")
                    .append("\t\t" + fullName + GEN_CLASS_SUFFIX + ".inject(arg);\n")
                    .append("\t}\n");

        }
        buffer.append("}");
//        生成ViewInjector类
        generateCode(INJECTOR_NAME, buffer.toString());
        return true;
    }

    /**
     * 生成代码
     * @param classname
     * @param code
     */
    private void generateCode(String classname, String code) {
        try {
            JavaFileObject file = mFiler.createSourceFile(classname);
            Writer mWriter = file.openWriter();
            mWriter.write(code);
            mWriter.close();
        } catch (IOException mE) {
            mE.printStackTrace();
        }
    }

    /**
     * 生成注入器的代码如MainActivity$Injector
     * @param mPackageName
     * @param mClassname
     * @param views
     */
    private void generateInjectorCode(String mPackageName, String mClassname, List<Element> views) {
        StringBuilder mBuilder = new StringBuilder();
        mBuilder.append("package " + mPackageName + ";\n\n")
                .append("public class " + mClassname + GEN_CLASS_SUFFIX + "{\n")
                .append("public static void inject(" + mClassname + " arg){\n");
//        对每个View 遍历。生成findViewByid代码
        for (Element mElement : views) {
            String type = mElement.asType().toString();
            String name = mElement.getSimpleName().toString();
            int id = mElement.getAnnotation(InjectView.class).value();
            mBuilder.append("\t\targ." + name + "=(" + type + ")arg.findViewById(" + id + ");\n");

        }
        mBuilder.append("\t}\n").append("}");
        generateCode(mClassname + GEN_CLASS_SUFFIX, mBuilder.toString());
    }

    /**
     * 向mElemtMap中存入，elemt和List的键值对
     * @param mElemtMap
     * @param mClazz
     * @param mElement
     */
    private void addElment(Map<Element, List<Element>> mElemtMap, Element mClazz, Element mElement) {
        List<Element> list = mElemtMap.get(mClazz);
        if (list == null) {
            list = new ArrayList<>();
            mElemtMap.put(mClazz, list);
        }
        list.add(mElement);
    }
//判断是否是View的子类
    private boolean isView(TypeMirror mTypeMirror) {
        List<? extends TypeMirror> supers = mTypeUtils.directSupertypes(mTypeMirror);
        if (supers.size() == 0) {
            return false;

        }
        for (TypeMirror supertype :
                supers) {
            if (supertype.toString().equals("android.view.View") || isView(supertype)) {
                return true;
            }
        }
        return false;
    }
}
