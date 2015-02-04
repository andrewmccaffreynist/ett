package gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.edge.mu2

import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.database.xdr.XDRSimulatorInterface
import gov.nist.healthcare.ttt.database.xdr.XDRTestStepInterface
import gov.nist.healthcare.ttt.webapp.xdr.core.TestCaseExecutor
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseBuilder
import gov.nist.healthcare.ttt.webapp.xdr.domain.TestCaseEvent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.StandardContent
import gov.nist.healthcare.ttt.webapp.xdr.domain.testcase.TestCase
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by gerardin on 10/27/14.
 */
@Component
final class TestCase20b extends TestCase {

    final public String badEndpoint = id+"_badEndpoint"

    @Autowired
    public TestCase20b(TestCaseExecutor ex) {
        super(ex)
        XDRSimulatorInterface sim1 = registerGlobalEndpoints(badEndpoint, new HashMap())
        endpoints = [sim1.endpoint, sim1.endpointTLS]
    }

    @Override
    TestCaseEvent configure(String tcid, Map context, String username) {

        XDRTestStepInterface step = executor.executeDirectAddressCorrelationStep(tcid, context.direct_from)

        //Create a new test record.
        XDRRecordInterface record = new TestCaseBuilder(tcid, username).addStep(step).build()

        executor.db.addNewXdrRecord(record)

        log.info "test case ${tcid} : successfully configured. Ready to receive messages."

        def content = new StandardContent()

        return new TestCaseEvent(XDRRecordInterface.CriteriaMet.MANUAL, content)
    }

    @Override
    public void notifyXdrReceive(XDRRecordInterface record, TkValidationReport report) {

        XDRTestStepInterface step

        if (report.simId == goodEndpoint) {
            step = executor.executeSendProcessedMDN(report)
        } else if (report.simId == badEndpoint) {
            step = executor.executeSendFailureMDN(report)
        } else {
            throw new Exception("problem in the workflow")
        }

        record = new TestCaseBuilder(record).addStep(step).build()

        record.criteriaMet = XDRRecordInterface.CriteriaMet.MANUAL

        executor.db.updateXDRRecord(record)
        executor.db.updateXDRRecord(record)
        executor.db.updateXDRRecord(record)

        done(XDRRecordInterface.CriteriaMet.MANUAL, record)

    }
}
