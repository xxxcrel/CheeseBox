package beer.cheese;

import org.junit.Test;

public class FinalTest {

    @Test
    public void testFinal(){
        System.out.println(returnValue());
    }

    public int returnValue(){
        int a = 3;
        try {
            a = 5;
            return a;
        }catch (Exception e){
            a = 2;
            return a;
        }finally {
            a = 1;
//            return a;
        }
    }
}
