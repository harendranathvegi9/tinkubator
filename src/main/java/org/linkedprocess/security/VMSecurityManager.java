package org.linkedprocess.security;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Author: josh
 * Date: Jul 7, 2009
 * Time: 11:39:50 AM
 */
public class VMSecurityManager extends SecurityManager {

    public enum PermissionType {
        permission("permission"),
        createClassLoader("create_class_loader"),
        access("access"),
        exit("exit"),
        exec("exec"),
        link("link"),
        read("read"),
        write("write"),
        delete("delete"),
        connect("connect"),
        listen("listen"),
        accept("accept"),
        multicast("multicast"),
        propertiesAccess("properties_access"),
        propertyAccess("property_access"),
        printJobAccess("print_job_access"),
        systemClipboardAccess("system_clipboard_access"),
        awtEventQueueAccess("awt_event_queue_access"),
        packageAccess("package_access"),
        packageDefinition("package_definition"),
        setFactory("set_factory"),
        memberAccess("member_access"),
        securityAccess("security_access");

        private final String specName;

        private PermissionType(final String specName) {
            this.specName = specName;
        }

        public boolean isPermitted(final Properties props) {
            return Boolean.valueOf(props.getProperty(getPropertyName()));
        }

        public static Set<PermissionType> createSet(final Properties props) {
            Set<PermissionType> set = new HashSet<PermissionType>();

            for (PermissionType pt : PermissionType.values()) {
                if (pt.isPermitted(props)) {
                    set.add(pt);
                }
            }

            return set;
        }

        public String getPropertyName() {
            return "org.linkedprocess.security." + this;
        }

        public String getSpecName() {
            return specName;
        }
    }

    private final Set<PermissionType> permittedTypes;

    private PathPermissions
            readPermissions,
            writePermissions,
            execPermissions,
            linkPermissions,
            httpGetPermissions,
            httpPutPermissions,
            httpPostPermissions;

    private boolean isVMWorkerThread() {
        // This is weird, but the below (commented out) results in a ClassCircularityError due to permissions associated with class comparison.
        return Thread.currentThread().toString() == VMSandboxedThread.SPECIAL_TOSTRING_VALUE;

        //return Thread.currentThread().getClass() == VMSandboxedThread.class;
    }

    private void permissionDenied() {
        throw new SecurityException("operation is not allowed in VM worker threads");
    }

    private void permissionDenied(final PermissionType type) {
        throw new SecurityException("operation type is not allowed in VM worker threads: " + type);
    }

    private void permissionDenied(final PermissionType type, final String resource) {
        throw new SecurityException("permission '" + type + "' is not granted to resource: " + resource);
    }

    private void checkPermissionType(final PermissionType type) {
        if (!permittedTypes.contains(type)) {
            permissionDenied(type);
        }
    }

    public VMSecurityManager(final Properties props) {
        permittedTypes = PermissionType.createSet(props);
    }

    ////////////////////////////////////////////////////////////////////////////
    
    public Set<PermissionType> getPermittedTypes() {
        return permittedTypes;
    }

    public PathPermissions getReadPermissions() {
        return readPermissions;
    }

    public PathPermissions getWritePermissions() {
        return writePermissions;
    }

    public PathPermissions getExecPermissions() {
        return execPermissions;
    }

    public PathPermissions getLinkPermissions() {
        return linkPermissions;
    }

    ////////////////////////////////////////////////////////////////////////////

    public void setReadPermissions(final PathPermissions p) {
        readPermissions = p;
    }

    public void setWritePermissions(final PathPermissions p) {
        writePermissions = p;
    }

    public void setExecPermissions(final PathPermissions p) {
        execPermissions = p;
    }

    public void setLinkPermissions(final PathPermissions p) {
        linkPermissions = p;
    }

    public void setHttpGetPermissions(final PathPermissions p) {
        httpGetPermissions = p;
    }

    public void setHttpPutPermissions(final PathPermissions p) {
        httpPutPermissions = p;
    }

    public void setHttpPostPermissions(final PathPermissions p) {
        httpPostPermissions = p;
    }

    @Override
    public void checkPermission(final Permission permission) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.permission);
        }
    }

    @Override
    public void checkPermission(final Permission permission,
                                final Object o) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.permission);
        }
    }

    @Override
    public void checkCreateClassLoader() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.createClassLoader);
        }
    }

    @Override
    public void checkAccess(final Thread thread) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.access);
        }
    }

    @Override
    public void checkAccess(final ThreadGroup threadGroup) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.access);
        }
    }

    @Override
    public void checkExit(final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.exit);
        }
    }

    @Override
    public void checkExec(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.exec);
            if (null == execPermissions || !execPermissions.isPermitted(s)) {
                permissionDenied(PermissionType.exec, s);
            }
        }
    }

    @Override
    public void checkLink(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.link);
            if (null == linkPermissions || !linkPermissions.isPermitted(s)) {
                permissionDenied(PermissionType.link, s);
            }
        }
    }

    @Override
    public void checkRead(final FileDescriptor fileDescriptor) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.read);
            // Deny anyway...
            permissionDenied();
        }
    }

    @Override
    public void checkRead(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.read);
            if (null == readPermissions || !readPermissions.isPermitted(s)) {
                permissionDenied(PermissionType.read, s);
            }
        }
    }

    @Override
    public void checkRead(final String s,
                          final Object o) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.read);
            // Deny anyway...
            permissionDenied();
        }
    }

    @Override
    public void checkWrite(final FileDescriptor fileDescriptor) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.write);
            // Deny anyway...
            permissionDenied();
        }
    }

    @Override
    public void checkWrite(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.write);
            if (null == writePermissions || !writePermissions.isPermitted(s)) {
                permissionDenied(PermissionType.write, s);
            }
        }
    }

    @Override
    public void checkDelete(final String s) {
        // For now, write permission implies delete permission.
        checkWrite(s);
    }

    @Override
    public void checkConnect(final String s,
                             final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.connect);
        }
    }

    @Override
    public void checkConnect(final String s,
                             final int i,
                             final Object o) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.connect);
        }
    }

    @Override
    public void checkListen(final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.listen);
        }
    }

    @Override
    public void checkAccept(final String s,
                            final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.accept);
        }
    }

    @Override
    public void checkMulticast(final InetAddress inetAddress) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.multicast);
        }
    }

    @Override
    public void checkPropertiesAccess() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.propertiesAccess);
        }
    }

    @Override
    public void checkPropertyAccess(java.lang.String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.propertyAccess);
        }
    }

    //public boolean checkTopLevelWindow(java.lang.Object o) {  }

    @Override
    public void checkPrintJobAccess() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.printJobAccess);
        }
    }

    @Override
    public void checkSystemClipboardAccess() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.systemClipboardAccess);
        }
    }

    @Override
    public void checkAwtEventQueueAccess() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.awtEventQueueAccess);
        }
    }

    @Override
    public void checkPackageAccess(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.packageAccess);
        }
    }

    @Override
    public void checkPackageDefinition(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.packageDefinition);
        }
    }

    @Override
    public void checkSetFactory() {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.setFactory);
        }
    }

    @Override
    public void checkMemberAccess(final Class<?> aClass,
                                  final int i) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.memberAccess);
        }
    }

    @Override
    public void checkSecurityAccess(final String s) {
        if (isVMWorkerThread()) {
            checkPermissionType(PermissionType.securityAccess);
        }
    }
}
