package assemblysequenceapi;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.JsonServerMethod;
import us.kbase.common.service.JsonServerServlet;
import us.kbase.common.service.JsonServerSyslog;
import us.kbase.common.service.RpcContext;

//BEGIN_HEADER
//END_HEADER

/**
 * <p>Original spec-file module name: AssemblySequenceAPI</p>
 * <pre>
 * A KBase module: AssemblySequenceAPI
 * </pre>
 */
public class AssemblySequenceAPIServer extends JsonServerServlet {
    private static final long serialVersionUID = 1L;
    private static final String version = "0.0.1";
    private static final String gitUrl = "";
    private static final String gitCommitHash = "";

    //BEGIN_CLASS_HEADER
    public AssemblySequenceAPIImpl impl = null;
    //END_CLASS_HEADER

    public AssemblySequenceAPIServer() throws Exception {
        super("AssemblySequenceAPI");
        //BEGIN_CONSTRUCTOR
        this.impl = new AssemblySequenceAPIImpl(config);
        //END_CONSTRUCTOR
    }

    /**
     * <p>Original spec-file function name: get_dna_sequences</p>
     * <pre>
     * </pre>
     * @param   params   instance of type {@link assemblysequenceapi.GetDNASequencesParams GetDNASequencesParams}
     * @return   instance of type {@link assemblysequenceapi.GetDNASequencesOutput GetDNASequencesOutput}
     */
    @JsonServerMethod(rpc = "AssemblySequenceAPI.get_dna_sequences", async=true)
    public GetDNASequencesOutput getDnaSequences(GetDNASequencesParams params, AuthToken authPart, RpcContext jsonRpcContext) throws Exception {
        GetDNASequencesOutput returnVal = null;
        //BEGIN get_dna_sequences
        returnVal = this.impl.getDnaSequences(params, authPart);
        //END get_dna_sequences
        return returnVal;
    }
    @JsonServerMethod(rpc = "AssemblySequenceAPI.status")
    public Map<String, Object> status() {
        Map<String, Object> returnVal = null;
        //BEGIN_STATUS
        returnVal = new LinkedHashMap<String, Object>();
        returnVal.put("state", "OK");
        returnVal.put("message", "");
        returnVal.put("version", version);
        returnVal.put("git_url", gitUrl);
        returnVal.put("git_commit_hash", gitCommitHash);
        //END_STATUS
        return returnVal;
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            new AssemblySequenceAPIServer().startupServer(Integer.parseInt(args[0]));
        } else if (args.length == 3) {
            JsonServerSyslog.setStaticUseSyslog(false);
            JsonServerSyslog.setStaticMlogFile(args[1] + ".log");
            new AssemblySequenceAPIServer().processRpcCall(new File(args[0]), new File(args[1]), args[2]);
        } else {
            System.out.println("Usage: <program> <server_port>");
            System.out.println("   or: <program> <context_json_file> <output_json_file> <token>");
            return;
        }
    }
}
