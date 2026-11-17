import java.util.*;
class Patient {
    final String name; final int severity; final long arrival;
    Patient(String n,int s,long a){name=n;severity=s;arrival=a;}
    @Override public String toString(){ return name+"(S"+severity+")"; }
}
public class Triage {
    private long counter=0;
    private final PriorityQueue<Patient> queue=new PriorityQueue<>(Comparator.comparingInt((Patient p)->p.severity).thenComparingLong(p->p.arrival));
    void admit(String name,int severity){ queue.add(new Patient(name,severity,counter++)); }
    Patient next(){ return queue.poll(); }
    List<Patient> waiting(){ List<Patient> l=new ArrayList<>(queue); l.sort(Comparator.comparingInt((Patient p)->p.severity).thenComparingLong(p->p.arrival)); return l; }
    public static void main(String[] a){
        Triage er=new Triage();
        er.admit("Ravi",3); er.admit("Meena",1); er.admit("John",3); er.admit("Asha",5); er.admit("Kumar",1); er.admit("Lata",2);
        StringBuilder order=new StringBuilder(); Patient p;
        while((p=er.next())!=null) order.append(p).append(' ');
        System.out.println(order.toString().trim());
        // hand-verifiable 8 patients
        Triage t=new Triage(); String[][] d={{"P1","4"},{"P2","2"},{"P3","5"},{"P4","2"},{"P5","1"},{"P6","4"},{"P7","1"},{"P8","3"}};
        for(String[] x:d) t.admit(x[0],Integer.parseInt(x[1]));
        System.out.println("PriorityQueue printed directly: "+t.queue);
        System.out.println("waiting() sorted view         : "+t.waiting());
        StringBuilder o=new StringBuilder(); while((p=t.next())!=null) o.append(p).append(' ');
        System.out.println("treatment order               : "+o.toString().trim());
        System.out.println("hand-worked expected          : P5(S1) P7(S1) P2(S2) P4(S2) P8(S3) P1(S4) P6(S4) P3(S5)");
        // growth: add and poll cost
        System.out.println("n,add_total_ms,ns_per_add,poll_total_ms,ns_per_poll,ns_per_poll/log2(n)");
        for(int n:new int[]{1000,10000,100000,1000000}){
            double ad=0,po=0; int R=5; Random r=new Random(5);
            for(int k=0;k<R+2;k++){
                Triage e=new Triage(); int[] sev=new int[n]; for(int i=0;i<n;i++) sev[i]=1+r.nextInt(5);
                long t0=System.nanoTime(); for(int i=0;i<n;i++) e.admit("X",sev[i]); long t1=System.nanoTime();
                while(e.next()!=null); long t2=System.nanoTime();
                if(k>=2){ad+=(t1-t0)/1e6; po+=(t2-t1)/1e6;}
            }
            double nsAdd=ad/R*1e6/n, nsPoll=po/R*1e6/n;
            System.out.printf("%d,%.2f,%.1f,%.2f,%.1f,%.2f%n",n,ad/R,nsAdd,po/R,nsPoll,nsPoll/(Math.log(n)/Math.log(2)));
        }
        // alternatives: sorted LinkedList insertion, 5 FIFO queues
        System.out.println("-- alternatives: admit n patients then serve all (ms, avg of 5) --");
        for(int n:new int[]{1000,10000,50000}){
            double pq=0,ll=0,fq=0; int R=5; Random r=new Random(9); int[] sev=new int[n]; for(int i=0;i<n;i++) sev[i]=1+r.nextInt(5);
            for(int k=0;k<R+2;k++){
                long t0=System.nanoTime(); Triage e=new Triage(); for(int i=0;i<n;i++) e.admit("X",sev[i]); while(e.next()!=null); long t1=System.nanoTime();
                LinkedList<Patient> L=new LinkedList<>(); for(int i=0;i<n;i++){ Patient np=new Patient("X",sev[i],i); ListIterator<Patient> it=L.listIterator(); while(it.hasNext()){ if(it.next().severity>np.severity){ it.previous(); break; } } it.add(np);} while(L.pollFirst()!=null); long t2=System.nanoTime();
                @SuppressWarnings("unchecked") ArrayDeque<Patient>[] F=new ArrayDeque[6]; for(int i=1;i<=5;i++) F[i]=new ArrayDeque<>(); for(int i=0;i<n;i++) F[sev[i]].addLast(new Patient("X",sev[i],i)); for(int s=1;s<=5;s++) while(F[s].pollFirst()!=null); long t3=System.nanoTime();
                if(k>=2){pq+=(t1-t0)/1e6; ll+=(t2-t1)/1e6; fq+=(t3-t2)/1e6;}
            }
            System.out.printf("n=%d: PriorityQueue %.2f | sorted LinkedList %.2f | 5 FIFO queues %.2f%n",n,pq/R,ll/R,fq/R);
        }
        // 5000 patients/day
        long t0=System.nanoTime(); Triage day=new Triage(); Random r=new Random(1); for(int i=0;i<5000;i++) day.admit("X",1+r.nextInt(5)); while(day.next()!=null); System.out.printf("5000 patients admit+serve (cold JVM, single run): %.2f ms%n",(System.nanoTime()-t0)/1e6);
        // starvation demo
        Triage s=new Triage(); s.admit("OldMinor",5); for(int i=0;i<1000;i++){ s.admit("Crit"+i,1); s.next(); }
        System.out.println("starvation: after 1000 critical arrivals each served immediately, OldMinor still waiting = "+s.waiting());
    }
}
