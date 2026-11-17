import java.lang.management.ManagementFactory;
public class TicketBench {
    static final com.sun.management.ThreadMXBean MX = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    static String[] fields(int n){ String[] f=new String[n]; for(int i=0;i<n;i++) f[i]="FIELD"+i+"-ABCDE"; return f; }
    static String plus(String[] f){ String s=""; for(String x:f) s = s + x + "|"; return s; }
    static String sb(String[] f){ StringBuilder b=new StringBuilder(); for(String x:f) b.append(x).append('|'); return b.toString(); }
    static String sbuf(String[] f){ StringBuffer b=new StringBuffer(); for(String x:f) b.append(x).append('|'); return b.toString(); }
    interface B { String run(String[] f); }
    static void bench(String w,int fields,int tickets,String name,B b){
        String[] f=fields(fields); long sink=0;
        for(int i=0;i<2;i++) for(int t=0;t<tickets;t++) sink+=b.run(f).length();
        double tot=0; long bytes=0;
        for(int r=0;r<5;r++){
            long a0=MX.getThreadAllocatedBytes(Thread.currentThread().getId());
            long t0=System.nanoTime();
            for(int t=0;t<tickets;t++) sink+=b.run(f).length();
            tot+=(System.nanoTime()-t0)/1e6;
            bytes=MX.getThreadAllocatedBytes(Thread.currentThread().getId())-a0;
        }
        System.out.printf("%s,%s,%d,%d,%.2f,%.1f,%d%n",w,name,fields,tickets,tot/5,bytes/1048576.0,sink>0?1:0);
    }
    public static void main(String[] a) throws Exception {
        System.out.println("workload,approach,fields,tickets,avg_ms(5 runs),alloc_MB_per_run,_");
        int[][] W={{10,100000},{50,10000},{1000,200}};
        String[] wn={"W1","W2","W3"};
        for(int i=0;i<3;i++){
            bench(wn[i],W[i][0],W[i][1],"String+",TicketBench::plus);
            bench(wn[i],W[i][0],W[i][1],"StringBuilder",TicketBench::sb);
            bench(wn[i],W[i][0],W[i][1],"StringBuffer",TicketBench::sbuf);
        }
        // shared log with 4 threads
        for(String kind: new String[]{"StringBuilder","StringBuffer"}){
            int bad=0, exc=0; int runs=20;
            for(int r=0;r<runs;r++){
                Appendable dummy=null;
                final StringBuilder sb1=new StringBuilder(); final StringBuffer sf=new StringBuffer();
                Thread[] ts=new Thread[4]; final int[] ex={0};
                for(int i=0;i<4;i++){ ts[i]=new Thread(()->{ try{ for(int k=0;k<100000;k++){ if(kind.equals("StringBuilder")) sb1.append('x'); else sf.append('x'); } }catch(Throwable e){ synchronized(ex){ex[0]++;} } }); }
                for(Thread t:ts) t.start(); for(Thread t:ts) t.join();
                int len = kind.equals("StringBuilder")? sb1.length(): sf.length();
                if(len!=400000) bad++; if(ex[0]>0) exc++;
            }
            System.out.println("SHARED_LOG,"+kind+",runs="+runs+",wrong_length_runs="+bad+",runs_with_exception="+exc);
        }
    }
}
