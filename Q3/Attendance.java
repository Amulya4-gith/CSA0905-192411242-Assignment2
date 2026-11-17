import java.util.*;
class BadStudent {
    String regNo, name;
    BadStudent(String r,String n){regNo=r;name=n;}
    public boolean equals(Object o){ if(!(o instanceof BadStudent)) return false; return regNo.equals(((BadStudent)o).regNo); }
    public String toString(){ return regNo+" "+name; }
}
class MutStudent {
    String regNo, name;
    MutStudent(String r,String n){regNo=r;name=n;}
    public boolean equals(Object o){ return o instanceof MutStudent && regNo.equals(((MutStudent)o).regNo); }
    public int hashCode(){ return regNo.hashCode(); }
}
class Student {
    private final String regNo; private final String name;
    Student(String r,String n){regNo=r;name=n;}
    @Override public boolean equals(Object o){ if(this==o) return true; if(!(o instanceof Student)) return false; return regNo.equals(((Student)o).regNo); }
    @Override public int hashCode(){ return regNo.hashCode(); }
    @Override public String toString(){ return regNo+" "+name; }
}
public class Attendance {
    public static void main(String[] x){
        System.out.println("== Test 1: every scan is the SAME object (appears to work) ==");
        Set<BadStudent> s1=new HashSet<>(); BadStudent arun=new BadStudent("192311001","Arun");
        s1.add(arun); s1.add(new BadStudent("192311002","Divya")); s1.add(arun);
        System.out.println("size = "+s1.size()+"  (predicted 2)");
        System.out.println("== Test 2: same student scanned twice, NEW object each scan (no hashCode) ==");
        Set<BadStudent> s2=new HashSet<>();
        s2.add(new BadStudent("192311001","Arun")); s2.add(new BadStudent("192311002","Divya")); s2.add(new BadStudent("192311001","Arun"));
        System.out.println("size = "+s2.size()+"  (predicted 3)");
        System.out.println("contains(new 192311001) = "+s2.contains(new BadStudent("192311001","Arun")));
        System.out.println("== Test 2b: with TODO 1 completed (Attendance.java as given) ==");
        Set<Student> s3=new HashSet<>();
        s3.add(new Student("192311001","Arun")); s3.add(new Student("192311002","Divya")); s3.add(new Student("192311001","Arun"));
        System.out.println("Unique students: "+s3.size());
        System.out.println("== Test 3a: change regNo AFTER insertion (hashCode uses regNo) ==");
        Set<MutStudent> s4=new HashSet<>(); MutStudent m=new MutStudent("192311005","Kiran"); s4.add(m);
        m.regNo="192311099";
        System.out.println("size = "+s4.size()+", contains(m) = "+s4.contains(m)+", remove(m) = "+s4.remove(m)+", size after remove = "+s4.size()+"  (predicted: 1,false,false,1)");
        s4.add(m); System.out.println("add(m) again -> size = "+s4.size()+" (same object stored twice)");
        System.out.println("== Test 3b: TreeSet with compareTo inconsistent with equals ==");
        TreeSet<Student> t=new TreeSet<>(Comparator.comparing(s->s.toString().length()));
        t.add(new Student("192311001","Arun")); t.add(new Student("192311002","Divy"));
        System.out.println("TreeSet size = "+t.size()+" (different students, same length -> treated as duplicates; predicted 1)");
        // timing: HashSet vs TreeSet vs HashMap for n scans
        for(int n:new int[]{1000,10000,100000}){
            String[] ids=new String[n]; for(int i=0;i<n;i++) ids[i]="19"+(2300000+ (i*7919)%n);
            double h=0,tr=0,hm=0; int R=5;
            for(int r=0;r<R+2;r++){
                long t0=System.nanoTime(); Set<Student> hs=new HashSet<>(); for(String id:ids) hs.add(new Student(id,"N")); long t1=System.nanoTime();
                TreeSet<Student> ts=new TreeSet<>(Comparator.comparing(q->q.toString())); for(String id:ids) ts.add(new Student(id,"N")); long t2=System.nanoTime();
                Map<String,Student> mp=new HashMap<>(); for(String id:ids) mp.putIfAbsent(id,new Student(id,"N")); long t3=System.nanoTime();
                if(r>=2){h+=(t1-t0)/1e6; tr+=(t2-t1)/1e6; hm+=(t3-t2)/1e6;}
            }
            System.out.printf("SCANS n=%d: HashSet %.2f ms, TreeSet %.2f ms, HashMap %.2f ms%n",n,h/R,tr/R,hm/R);
        }
    }
}
