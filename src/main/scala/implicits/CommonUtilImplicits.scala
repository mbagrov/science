package implicits

object CommonUtilImplicits
{
    implicit class BoolImplicits(val b: Boolean) extends AnyVal
    {
        def ifTrue[R](fn: => R)(orElse: => R): R = if(b) fn else orElse
    }

}