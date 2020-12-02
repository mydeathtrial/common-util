package cloud.agileframework.common.util;

import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.date.DateUtil;
import cloud.agileframework.common.util.http.HttpUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.agile.common.data.DemoA;
import com.agile.common.data.DemoC;
import com.agile.common.data.DemoD;
import com.agile.common.data.DemoE;
import com.google.common.collect.Maps;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.AnnotationNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.LdcInsnNode;
import jdk.internal.org.objectweb.asm.tree.LocalVariableNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * @author 佟盟
 * 日期 2019/10/31 9:52
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class ObjectUtilTest {

    /**
     * 测试对象转Map
     */
    @Test
    public void objectToMap() {



        Map<String, Object> data = DemoA.testData();
        DemoA a = ObjectUtil.to(data, new TypeReference<DemoA>() {
        });
        long start = System.currentTimeMillis();
        IntStream.range(0,20).forEach(i->{
            ObjectUtil.to(data, new TypeReference<DemoA>() {
            });
        });

        System.out.println("总耗时："+(System.currentTimeMillis()-start));

//        Object jsona =   TypeUtils.cast(data,DemoA.class, ParserConfig.getGlobalInstance());
//        long start2 = System.currentTimeMillis();
//        IntStream.range(0,1000000).forEach(i->{
//            TypeUtils.cast(data,DemoA.class, ParserConfig.getGlobalInstance());
//        });
//
//        System.out.println("总耗时："+(System.currentTimeMillis()-start2));

        DemoA b = new DemoA();
        ObjectUtil.copyProperties(a,b);
        System.out.println();
    }

    /**
     * 测试对象转对象
     */
    @Test
    public void objectToObject() {
        DemoA demoA = ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        ObjectUtil.to(demoA, new TypeReference<DemoC>() {
        });
    }

    /**
     * 测试Map转对象
     */
    @Test
    public void parseDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GregorianCalendar date;
        date = DateUtil.parse("19900905 11:12 下午");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("19900905 pm 11:12");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("pm 1990-9-5 11:12");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("11:12 1990/09/05");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("11:2:03 1990年9月05");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("11:02:33 1990-09/05");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("1596782907410");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("6782907410");
        System.out.println(format.format(date.getTime()));
    }

    @Test
    public void parseDate2() {
        ObjectUtil.to(new ArrayList<String>() {{
            add("123");
        }}, new TypeReference<List>() {
        });
    }

    public static void main(String[] args) throws IOException {
//        ClassReader reader = new ClassReader(DemoA.class.getCanonicalName());
//        ClassNode cn = new ClassNode();
//        reader.accept(cn, 0);
//        System.out.println(cn.name);
//        List<FieldNode> fieldList = cn.fields;
//        for (FieldNode fieldNode : fieldList) {
//            System.out.println("Field name: " + fieldNode.name);
//            System.out.println("Field desc: " + fieldNode.desc);
//            System.out.println("Filed value: " + fieldNode.value);
//            System.out.println("Filed access: " + fieldNode.access);
//        }

        ClassReader reader = new ClassReader(DemoA.class.getCanonicalName());
        ClassNode cn = new ClassNode();
        reader.accept(cn, 0);
        List<MethodNode> methodList = cn.methods;
        for (MethodNode md : methodList) {
            System.out.println(md.name);
            System.out.println(md.access);
            System.out.println(md.desc);
            System.out.println(md.signature);
            List<LocalVariableNode> lvNodeList = md.localVariables;
            for (LocalVariableNode lvn : lvNodeList) {
                System.out.println("Local name: " + lvn.name);
                System.out.println("Local name: " + lvn.start.getLabel());
                System.out.println("Local name: " + lvn.desc);
                System.out.println("Local name: " + lvn.signature);
            }
            Iterator<AbstractInsnNode> instraIter = md.instructions.iterator();
            while (instraIter.hasNext()) {
                AbstractInsnNode abi = instraIter.next();
                if (abi instanceof LdcInsnNode) {
                    LdcInsnNode ldcI = (LdcInsnNode) abi;
                    System.out.println("LDC node value: " + ldcI.cst);
                }
            }
        }
        MethodVisitor mv = cn.visitMethod(Opcodes.AALOAD, "<init>", Type
                .getType(String.class).toString(), null, null);
        mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(String.class), "str", Type
                .getType(String.class).toString());
        System.out.println(cn.name);
        List<FieldNode> fieldList = cn.fields;
        for (FieldNode fieldNode : fieldList) {
            System.out.println("Field name: " + fieldNode.name);
            System.out.println("Field desc: " + fieldNode.desc);
            System.out.println("Filed value: " + fieldNode.value);
            System.out.println("Filed access: " + fieldNode.access);
            if (fieldNode.visibleAnnotations != null) {
                for (Object anNode : fieldNode.invisibleAnnotations) {
                    System.out.println(((AnnotationNode)anNode).desc);
                }
            }
        }
    }

//    public static void main(String[] args) {
//        boolean flag=true;
//        int n=0;
//        Stack stack=new Stack();
//        Scanner in=new Scanner(System.in);
//        System.out.println("请输入只包含\"(\",\")\"的字符串！");
//        String string=in.nextLine();
//        System.out.println("输入的字符串为:"+string);
//        char[] s=string.toCharArray();
//        for(int i=0;i<s.length;i++) {
//            if(s[i]=='(') {
//                stack.push(s[i]);
//            }
//            else {
//                if(!stack.isEmpty()) {
//                    stack.pop();
//                    n++;
//                }
//
//            }
//        }
//        System.out.println("最长有效括号的字串的长度为："+n*2);
//    }

    @Test
    public void getAliasInfo(){
        Map<Field, Set<Field>> s = ObjectUtil.getSameFieldByAlias(DemoD.class, DemoE.class,"","");
        System.out.println(s);
    }


    @Test
    public void copyByAlias(){
        DemoE e = new DemoE();
        ObjectUtil.copyProperties(DemoD.builder().paramA("2").paramB(10).build(),e,true);
        System.out.println(e);
    }

    @Test
    public void https(){
        Map<String,String> map = Maps.newHashMap();
        map.put("Accept","application/json");
        map.put("Content-type","application/json");
//        map.put("token","ODULjdxyL99dzcSfcJ6zxNHGzivsaGAUC6i4Xt_7tT5P0bqf4Fa6gCizIhNrmxLh3wtjYFba3rfdi7I7Rvg3vg");
        String a = HttpUtil.post("https://192.168.50.174:18081/api/holmes/scanFile", map,null);
        System.out.println(a);
    }
}
