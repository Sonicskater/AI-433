package Tree;

import DataClass.*;

import java.util.*;

/* Hard constraints:
Not more than coursemax(s) courses can be assigned to slot s.
Not more than labmax(s) labs can be assigned to slot s.
For each of not-compatible(a,b) statements, assign(a) has to be unequal to assign(b).
For each of unwanted(a,s) statements, assign(a) has to be unequal to s.
Evening classes (LEC 9+) have to be scheduled into evening slots (18:00+).
All courses (course sections) on the 500-level have to be scheduled into different time slots.
Assign(ci) needs to be unequal to assign(lik) for all k and i.
There are two special "courses" CPSC 813 and CPSC 913 that have to be scheduled tuesdays/thursdays 18:00-19:00
 and CPSC 813 is not allowed to overlap with any labs/tutorials of CPSC 313 or with any course section of CPSC 313.
 and CPSC 913 is not allowed to overlap with any labs/tutorials of CPSC 413 or with any course section of CPSC 413.

//Constraint checked in constructors:
No courses can be scheduled at tuesdays 11:00-12:30.
*/


public class Constr {

    //Attributes
    private Set<NotCompatibleCoursePair> notCompatibles;
    private Set<UnwantedCourseTime> unwanted;

    //Constructors
    public Constr(){
        notCompatibles = new LinkedHashSet<>();
        unwanted = new LinkedHashSet<>();
    }

    public Constr(Set<NotCompatibleCoursePair> pairs, Set<UnwantedCourseTime> unwanted){
        notCompatibles = pairs;
        this.unwanted = unwanted;
    }

    //Functions
    public boolean constr(PSol sol){

        //Check if solution is complete
        if(! sol.getComplete())
            return false;
        else
            constrPartial(sol);

        return true;
    }

    public boolean constrPartial(PSol sol){

        Set<Slot> slots = sol.slotSet();
        //Special Courses conditions
        Course c813 = null, c913 = null;
        ArrayList<Course> noOverlap813 = new ArrayList<>();
        ArrayList<Course> noOverlap913 = new ArrayList<>();


        for (Slot slot : slots) {

            if (slot == null) {
                continue;
            }

            //Count how many courses and labs are assigned
            int coursenum = 0, labnum = 0;
            ArrayList<Course> courses500 = new ArrayList<>();
            for (Course course : sol.slotLookup(slot)) {
                if (course instanceof Lab) {
                    labnum++;

                    //Check if lab overlaps with its lecture
                    for (Section sec : ((Lab) course).getSections()){
                        if (slot.overlaps(sol.courseLookup(sec)))
                            return false;
                    }
                } else {
                    coursenum++;

                    //Check evening courses
                    if(((Section)course).getLecNum() >= 9) {
                        if ((int)slot.getStartTime() < 18)
                            return false;
                    }

                    //Count 500-level courses
                    if (course.getCourseNumber() >= 500) {
                        for (Course c : courses500) {
                            if (course.equals(c)) return false;
                        }
                        courses500.add(course);
                    }
                }

                //Special "course" CPSC 813 has to be scheduled tuesdays/thursdays 18:00-19:00
                if ((course.getCourseNumber() == 813) && (course.getCourseName().equals("CPSC"))){
                    if (! (slot.getDay().equals(Slot.Day.TU) && slot.getStartTime() == 18))
                        return false;
                    c813 = course;
                }
                //Special "course" CPSC 913 has to be scheduled tuesdays/thursdays 18:00-19:00
                if ((course.getCourseNumber() == 913) && (course.getCourseName().equals("CPSC"))){
                    if (! (slot.getDay().equals(Slot.Day.TU) && slot.getStartTime() == 18))
                        return false;
                    c913 = course;
                }
                if ((course.getCourseNumber() == 313) && (course.getCourseName().equals("CPSC")))
                    noOverlap813.add(course);
                if ((course.getCourseNumber() == 413) && (course.getCourseName().equals("CPSC")))
                    noOverlap913.add(course);
            } //for each course in slot

            //Check CourseMin and LabsMin
            if (slot instanceof CourseSlot) {
                if (coursenum > slot.getMax()) {
                    return false;
                }
            } else {
                if (labnum > slot.getMax()) {
                    return false;
                }
            }
        } //for each slot in sol

        //Check if a notCompatible pair has the same assignment
        for (NotCompatibleCoursePair pair : notCompatibles) {
            Slot s1 = sol.courseLookup(pair.getCourse1());
            Slot s2 = sol.courseLookup(pair.getCourse2());
            if ( (s1 != null) && (s2 != null) && (s1.equals(s2))) {
                return false;
            }
        }

        //Check if an Unwanted pair exists in sol
        for (UnwantedCourseTime pair : unwanted) {
            Course c = pair.getCourse();
            Slot s = pair.getSlot();
            if (sol.courseLookup(c).equals(s)) {
                return false;
            }
        }

        //Check if special courses overlap with unwanted overlaps
        //CPSC 813 is not allowed to overlap with any labs/tutorials of CPSC 313 or with any course section of CPSC 313.
        if (c813 != null){
            for (Course c : noOverlap913){
                if (sol.courseLookup(c).overlaps(sol.courseLookup(c813)))
                    return false;
            }
        }
        //CPSC 913 is not allowed to overlap with any labs/tutorials of CPSC 413 or with any course section of CPSC 413.
        if (c913 != null){
            for (Course c : noOverlap913){
                if (sol.courseLookup(c).overlaps(sol.courseLookup(c913)))
                    return false;
            }
        }

        return true;
    } //End of constrPartial

}
