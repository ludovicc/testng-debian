package priority;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PriorityInterceptor implements IMethodInterceptor {

  public List<IMethodInstance> intercept(List<IMethodInstance> methods,
      ITestContext context)
  {
    Comparator<IMethodInstance> comparator = new Comparator<IMethodInstance>() {
      
      private int getPriority(IMethodInstance mi) {
        int result = 0;
        Priority a1 = mi.getMethod().getMethod().getAnnotation(Priority.class);
        if (a1 != null) {
          result = a1.value();
        }
        return result;
      }

      public int compare(IMethodInstance m1, IMethodInstance m2) {
        return getPriority(m1) - getPriority(m2);
      }
      
    };
    IMethodInstance[] array = methods.toArray(new IMethodInstance[methods.size()]);
    Arrays.sort(array, comparator);

    return Arrays.asList(array);
  }

}
