package DataClass;

import java.util.Objects;

public class PreferredCoursePair
{
    private Course course1;
    private Course course2;

    public PreferredCoursePair(Course course1, Course course2)
    {
        this.course1 = course1;
        this.course2 = course2;
    }

    public Course getCourse1() {
        return course1;
    }

    public Course getCourse2() {
        return course2;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof NotCompatibleCoursePair)
        {
            NotCompatibleCoursePair them = (NotCompatibleCoursePair) o;
            return getCourse1().equals(them.getCourse1()) && getCourse2().equals(them.getCourse2());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(course1, course2);
    }
}
