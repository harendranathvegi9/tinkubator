/*
 * Copyright (c) 2009. The LoPSideD implementation of the Linked Process
 * protocol is an open-source project founded at the Center for Nonlinear Studies
 * at the Los Alamos National Laboratory in Los Alamos, New Mexico. Please visit
 * http://linkedprocess.org and LICENSE.txt for more information.
 */

package org.linkedprocess.xmpp.villein.commands;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.xmpp.LopError;
import org.linkedprocess.xmpp.villein.Handler;
import org.linkedprocess.xmpp.villein.XmppVillein;
import org.linkedprocess.xmpp.villein.proxies.JobStruct;
import org.linkedprocess.xmpp.villein.proxies.VmProxy;
import org.linkedprocess.xmpp.vm.PingJob;

/**
 * The proxy by which a ping_job is sent to a virtual machine.
 * Any result of the command is returned to the provided result handler.
 * Any error of the command is returned to the provided error handler.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class PingJobCommand extends Command {
    private final HandlerSet<LinkedProcess.JobStatus> resultHandlers;
    private final HandlerSet<LopError> errorHandlers;

    public PingJobCommand(XmppVillein xmppVillein) {
        super(xmppVillein);
        this.resultHandlers = new HandlerSet<LinkedProcess.JobStatus>();
        this.errorHandlers = new HandlerSet<LopError>();
    }

    public void send(VmProxy vmStruct, JobStruct jobStruct, final Handler<LinkedProcess.JobStatus> statusHandler, final Handler<LopError> errorHandler) {

        String id = Packet.nextID();
        PingJob pingJob = new PingJob();
        pingJob.setTo(vmStruct.getFullJid());
        pingJob.setFrom(this.xmppVillein.getFullJid());
        pingJob.setJobId(jobStruct.getJobId());
        pingJob.setVmPassword(vmStruct.getVmPassword());
        pingJob.setType(IQ.Type.GET);
        pingJob.setPacketID(id);

        this.resultHandlers.addHandler(id, statusHandler);
        this.errorHandlers.addHandler(id, errorHandler);

        xmppVillein.getConnection().sendPacket(pingJob);
    }

    public void receiveNormal(final PingJob pingJob) {
        try {
            resultHandlers.handle(pingJob.getPacketID(), LinkedProcess.JobStatus.valueOf(pingJob.getValue()));
        } finally {
            resultHandlers.removeHandler(pingJob.getPacketID());
            errorHandlers.removeHandler(pingJob.getPacketID());
        }
    }

    public void receiveError(final PingJob pingJob) {
        try {
            errorHandlers.handle(pingJob.getPacketID(), pingJob.getLopError());
        } finally {
            resultHandlers.removeHandler(pingJob.getPacketID());
            errorHandlers.removeHandler(pingJob.getPacketID());
        }
    }
}
