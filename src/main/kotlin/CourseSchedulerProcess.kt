import DataClass.PSol
import IO.ParsedData
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CourseSchedulerProcess(root: PSol): SearchProcess<CourseSchedulerTree, PSol>() {
    override fun execute(): PSol? {
        val start = System.currentTimeMillis()
        // ?: means return left, unelss its null then return right instead ( "?:" is known as the Elvis operator in kotlin (turn your screen))
        // putting ? in method clal chains says if this thing is null, thats fine, evaluate to null.
        // together this makes this happen:
        // return model.peekBest()?.data?.value ?: 1000000
        // is the same as:
        // val x = model.peekBest()
        // if ( x == null || x.data == null){
        //      return 1000000
        // }else{
        //      return x.data.value
        // }

        val count = AtomicInteger(0)
        val skipped = AtomicInteger(0)

        while (candidate== null && model.peekDeepest() !=null){
            count.incrementAndGet()
            fTrans(fLeafDepth())
        }
        model.depthFirst.clear()
        while (model.peekBest() != null && (System.currentTimeMillis()-start) < TimeUnit.MINUTES.toMillis(60*12)){

            while (model.peekBest()?.data?.value ?: 1000001 >= candidate?.value ?: 1000000) {
                model.best()
                skipped.incrementAndGet()
                count.incrementAndGet()
                if (model.peekBest() == null) {
                    break
                }
            }
            if (model.peekBest() == null) break
            count.incrementAndGet()
            fTrans(fLeafBest())
            //println("${model.queue.size} || ${model.depthFirst.size}")

        }
        println("Examined $count leaves, skipping $skipped. This means we skipped ${(skipped.get().toFloat()/count.get().toFloat())*100}%.")
        return candidate
    }

    private fun fLeafDepth(): AndTree<PSol>.Node? {
        return model.deepest()
    }

    private fun fLeafBest(): AndTree<PSol>.Node? {
        return model.best()
    }

    private fun fTrans(node: AndTree<PSol>.Node?) {
        node!!.expand()
        //println(node.depth.toString() +"||"+node?.data.value.toString()+ "||" + model.leaves.count() + "||" + node.data.courseSet().filter {node.data.courseLookup(it) != null }.count() +"/"+(ParsedData.COURSES.count()+ParsedData.LABS.count()))
        if (node.children.isEmpty()) {
            //node.solved = true
            //println("solved!")
        }

        // candidate?.value ?: 100000 explanation:
        // candidate?.value says IF candidate != null then get its value, otherwise return null, (no null pointer exception)
        // then ?: says if the left is null then return the right, in this case 100000.
        // so if candidate is null it goes: (candidate?.value) ?: 100000 -> (null) ?: 100000 -> 100000

        node.solved = node.data.complete
        if (node.solved && node.data.complete && node.data.value < (candidate?.value ?: 1000000000)) {
            candidate = node.data
            model.depthmode = false
            println(candidate?.value.toString()+ "||||" + candidate?.slotLookup(null) + "||" +candidate?.courseSet()?.count()+"/"+(ParsedData.COURSES.count()+ParsedData.LABS.count()))
            //println("New Candidate!")
        }

        node.children.forEach {
            it.solved = it.data.complete
            if (it.solved  && (it.data.value < (candidate?.value ?: 1000000000))) {
                candidate = it.data
                model.depthmode = false
                println(candidate?.value.toString()+ "||||" + candidate?.slotLookup(null) + "||" +candidate?.courseSet()?.filter { candidate?.courseLookup(it) != null }?.count()+"/"+(ParsedData.COURSES.count()+ParsedData.LABS.count()))

            }
            //println("Examined child!")
        }
        //println(candidate?.value.toString()+ "||" + model.leaves.count() + "||" + candidate?.slotLookup(null) + "||" +candidate?.courseSet()?.count()+"/"+(ParsedData.COURSES.count()+ParsedData.LABS.count()))
    }

    override val model: CourseSchedulerTree = CourseSchedulerTree(this,root)

}