/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.linkedprocess.LinkedProcess;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * User: marko
 * Date: Jun 25, 2009
 * Time: 1:21:38 PM
 */
public class AbortJobProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws IOException, XmlPullParserException {
        AbortJob abortJob = new AbortJob();
        String jobId = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.JOB_ID_ATTRIBUTE);
        if (null != jobId) {
            abortJob.setJobId(jobId);
        }
        String vmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_PASSWORD_ATTRIBUTE);
        if (null != vmPassword) {
            abortJob.setVmPassword(vmPassword);
        }
        parser.next();
        return abortJob;
    }
}
