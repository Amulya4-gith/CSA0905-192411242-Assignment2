import java.util.*;
public class ListBench {
    static long sink;
    interface Op { void run(); }
    static double time(Op o){ double best=Double.MAX_VALUE; double tot=0; int R=5; for(int r=0;r<R;r++){ long t=System.nanoTime(); o.run(); tot+=(System.nanoTime()-t)/1e6; } return tot/R; }
    static List<Integer> fill(List<Integer> l,int n){ for(int i=0;i<n;i++) l.add(i); return l; }
    public static void main(String[] a){
        int[] sizes={1000,10000,100000};
        int OPS=2000;
        System.out.println("op,size,ArrayList_ms,LinkedList_ms,ArrayDeque_ms  (avg of 5 runs; "+OPS+" ops except add-end/iterate)");
        for(int n:sizes){
            Random rnd=new Random(1); int[] idx=new int[OPS]; 
            // warmup
            for(int w=0;w<2;w++){ List<Integer> x=fill(new ArrayList<>(),n); List<Integer> y=fill(new LinkedList<>(),n); for(int i=0;i<100;i++){x.add(n/2,1);y.add(n/2,1);} }
            double[] r;
            // add at end (n adds)
            double a1=time(()->fill(new ArrayList<>(),n)), l1=time(()->fill(new LinkedList<>(),n)), d1=time(()->{ArrayDeque<Integer> d=new ArrayDeque<>(); for(int i=0;i<n;i++) d.addLast(i);});
            System.out.printf("add-end(n adds),%d,%.3f,%.3f,%.3f%n",n,a1,l1,d1);
            // insert at front
            List<Integer> AL=fill(new ArrayList<>(),n), LL=fill(new LinkedList<>(),n); ArrayDeque<Integer> DQ=new ArrayDeque<>(AL);
            double a2=time(()->{AL.add(0,1); for(int i=1;i<OPS;i++) AL.add(0,i); for(int i=0;i<OPS;i++) AL.remove(0);});
            double l2=time(()->{for(int i=0;i<OPS;i++) LL.add(0,i); for(int i=0;i<OPS;i++) LL.remove(0);});
            double d2=time(()->{for(int i=0;i<OPS;i++) DQ.addFirst(i); for(int i=0;i<OPS;i++) DQ.pollFirst();});
            System.out.printf("insert+remove front(%d each),%d,%.3f,%.3f,%.3f%n",OPS,n,a2,l2,d2);
            // middle
            double a3=time(()->{for(int i=0;i<OPS;i++) AL.add(AL.size()/2,i); for(int i=0;i<OPS;i++) AL.remove(AL.size()/2);});
            double l3=time(()->{for(int i=0;i<OPS;i++) LL.add(LL.size()/2,i); for(int i=0;i<OPS;i++) LL.remove(LL.size()/2);});
            System.out.printf("insert+remove middle(%d each),%d,%.3f,%.3f,n/a%n",OPS,n,a3,l3);
            // get k-th
            for(int i=0;i<OPS;i++) idx[i]=rnd.nextInt(n);
            double a4=time(()->{long s=0; for(int i=0;i<OPS;i++) s+=AL.get(idx[i]); sink+=s;});
            double l4=time(()->{long s=0; for(int i=0;i<OPS;i++) s+=LL.get(idx[i]); sink+=s;});
            System.out.printf("get k-th(%d random),%d,%.3f,%.3f,n/a%n",OPS,n,a4,l4);
            // iterate
            double a5=time(()->{long s=0; for(int v:AL) s+=v; sink+=s;});
            double l5=time(()->{long s=0; for(int v:LL) s+=v; sink+=s;});
            double d5=time(()->{long s=0; for(int v:DQ) s+=v; sink+=s;});
            System.out.printf("iterate all,%d,%.3f,%.3f,%.3f%n",n,a5,l5,d5);
        }
    }
}
