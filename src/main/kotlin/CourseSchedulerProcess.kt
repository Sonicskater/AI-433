import DataClass.PSol
import IO.ParsedData
import kotlinx.coroutines.*

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.coroutineContext

class CourseSchedulerProcess(root: PSol, private val duration_m: Long = 5): SearchProcess<CourseSchedulerTree, PSol>() {

    val mutex  = Mutex()
    override fun execute(): PSol? {
        // start time

        val start = System.currentTimeMillis()


        // ?: means return left, unless its null then return right instead ( "?:" is known as the Elvis operator in kotlin (turn your screen))
        // putting ? in method call chains says if this thing is null, that's fine, evaluate to null.
        // together this makes this happen:
        // return model.peekBest()?.data?.value ?: 1000000
        // is the same as:
        // val x = model.peekBest()
        // if ( x == null || x.data == null){
        //      return 1000000
        // }else{
        //      return x.data.value
        // }

        val duration = TimeUnit.MINUTES.toMillis(duration_m)

        // atomic in case we wanted to thread it
        val count = AtomicInteger(0)
        val skipped = AtomicInteger(0)


        // find initial candidate
        /*
        while (candidate== null && model.peekDeepest() !=null && (System.currentTimeMillis()-start) < duration){
            count.incrementAndGet()

            // do work
            fTrans(fLeafDepth())
        }
        */
        // deallocate depth first queue.


        // search for anything better.
        runBlocking {
            fTrans(fLeafBest())
        }
        val jobs = List(1){
            GlobalScope.async(Dispatchers.Default){
                println(it)
                while (model.peekBest() != null && (System.currentTimeMillis() - start) < duration) {

                    // skip any bad nodes.
                    while (model.peekBest()?.data?.value ?: 1000001 >= candidate?.value ?: 1000000) {
                        model.best()
                        skipped.incrementAndGet()
                        count.incrementAndGet()
                        if (model.peekBest() == null) {
                            break
                        }
                    }
                    // quit if we run out of nodes.
                    if (model.peekBest() == null) break
                    count.incrementAndGet()

                    // do work
                    fTrans(fLeafBest())
                }
                println("$it done")
            }
        }
        runBlocking {
            jobs.forEach { it.await() }
        }
        println("Examined $count leaves, skipping $skipped. This means we skipped ${(skipped.get().toFloat()/count.get().toFloat())*100}%.")
        return candidate
    }



    private fun fLeafBest(): AndTree<PSol>.Node? {
        return model.best()
    }

    private suspend fun asyncUpdate(sol: PSol){
        mutex.withLock {
            if (candidate?.value ?: 1000000 >= sol.value) candidate = sol
        }
    }

    private suspend fun fTrans(node: AndTree<PSol>.Node?) {

        node!!.expand()

        //println(node.data.value)

        // candidate?.value ?: 100000 explanation:
        // candidate?.value says IF candidate != null then get its value, otherwise return null, (no null pointer exception)
        // then ?: says if the left is null then return the right, in this case 100000.
        // so if candidate is null it goes: (candidate?.value) ?: 100000 -> (null) ?: 100000 -> 100000


        // examine the current node.
        node.solved = node.data.complete
        if (node.solved && node.data.complete && node.data.value < (candidate?.value ?: 1000000000)) {
            mutex.withLock {
                candidate = node.data
            }
            model.depthmode = false
            println(candidate?.value.toString()+ "||||" + candidate?.slotLookup(null) + "||" +candidate?.courseSet()?.count()+"/"+(ParsedData.COURSES.count()+ParsedData.LABS.count()))
        }

        // examine the children
        node.children.forEach {
            it.solved = it.data.complete
            if (it.solved  && (it.data.value < (candidate?.value ?: 1000000000))) {
                coroutineScope {
                    launch{
                        asyncUpdate(node.data)
                    }
                }
                model.depthmode = false
                println(candidate?.value.toString()+ "||||" + candidate?.slotLookup(null) + "||" +candidate?.courseSet()?.filter { candidate?.courseLookup(it) != null }?.count()+"/"+(ParsedData.COURSES.count()+ParsedData.LABS.count()))

            }
        }
    }

    override val model: CourseSchedulerTree = CourseSchedulerTree(this,root)

}