package DataClass
// Data class automatically creates == and copy constructors that evaluate the fields instead of the reference.
// makes the class struct-like.
data class PSol(val assignments: List<Assignment>) {


    // creates ocerloaded function so java coded can use the default parameters.
    @JvmOverloads
    // creates new copy of the pSol that has one item reassigned.
    // .minus().plus() creates new copy with 1 element replaced.

    fun copyEdit(new: Assignment, original: Assignment = Assignment(new.course, null)) : PSol {
        return PSol(assignments.minus(original).plus(new))
    }
}