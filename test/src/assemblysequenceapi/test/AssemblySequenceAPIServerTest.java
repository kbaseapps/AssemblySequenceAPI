package assemblysequenceapi.test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import assemblysequenceapi.AssemblySequenceAPIServer;
import assemblysequenceapi.FastaReader;
import assemblysequenceapi.GetDNASequencesOutput;
import assemblysequenceapi.GetDNASequencesParams;
import assemblyutil.AssemblyUtilClient;
import assemblyutil.FastaAssemblyFile;
import assemblyutil.SaveAssemblyParams;
import us.kbase.auth.AuthToken;
import us.kbase.auth.AuthService;
import us.kbase.common.service.JsonServerSyslog;
import us.kbase.common.service.RpcContext;
import us.kbase.common.service.Tuple4;
import us.kbase.common.service.UObject;
import us.kbase.kbasegenomes.Contig;
import us.kbase.kbasegenomes.ContigSet;
import us.kbase.kbasegenomes.Feature;
import us.kbase.kbasegenomes.Genome;
import us.kbase.workspace.CreateWorkspaceParams;
import us.kbase.workspace.ObjectSaveData;
import us.kbase.workspace.ProvenanceAction;
import us.kbase.workspace.SaveObjectsParams;
import us.kbase.workspace.WorkspaceClient;
import us.kbase.workspace.WorkspaceIdentity;

public class AssemblySequenceAPIServerTest {
    private static AuthToken token = null;
    private static Map<String, String> config = null;
    private static WorkspaceClient wsClient = null;
    private static String wsName = null;
    private static AssemblySequenceAPIServer impl = null;
    
    @BeforeClass
    public static void init() throws Exception {
        //TODO AUTH make configurable?
        token = AuthService.validateToken(System.getenv("KB_AUTH_TOKEN"));
        String configFilePath = System.getenv("KB_DEPLOYMENT_CONFIG");
        File deploy = new File(configFilePath);
        Ini ini = new Ini(deploy);
        config = ini.get("AssemblySequenceAPI");
        wsClient = new WorkspaceClient(new URL(config.get("workspace-url")), token);
        wsClient.setIsInsecureHttpConnectionAllowed(true);
        // These lines are necessary because we don't want to start linux syslog bridge service
        JsonServerSyslog.setStaticUseSyslog(false);
        JsonServerSyslog.setStaticMlogFile(new File(config.get("scratch"), "test.log").getAbsolutePath());
        impl = new AssemblySequenceAPIServer();
    }
    
    private static String getWsName() throws Exception {
        if (wsName == null) {
            long suffix = System.currentTimeMillis();
            wsName = "test_AssemblySequenceAPI_" + suffix;
            wsClient.createWorkspace(new CreateWorkspaceParams().withWorkspace(wsName));
        }
        return wsName;
    }
    
    private static RpcContext getContext() {
        return new RpcContext().withProvenance(Arrays.asList(new ProvenanceAction()
            .withService("AssemblySequenceAPI").withMethod("please_never_use_it_in_production")
            .withMethodParams(new ArrayList<UObject>())));
    }
    
    @AfterClass
    public static void cleanup() {
        if (wsName != null) {
            try {
                wsClient.deleteWorkspace(new WorkspaceIdentity().withWorkspace(wsName));
                System.out.println("Test workspace was deleted");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    @Test
    public void testGetDnaSequencesForAssembly() throws Exception {
        File tempFastaFile = new File("/kb/module/work/tmp/GCF_000002945.fa");
        FileUtils.copyFile(new File("/kb/module/test/data/GCF_000002945/assembly.fa"), tempFastaFile);
        URL callbackUrl = new URL(System.getenv("SDK_CALLBACK_URL"));
        AssemblyUtilClient auCl = new AssemblyUtilClient(callbackUrl, token);
        auCl.setIsInsecureHttpConnectionAllowed(true);
        String assemblyName = "Assembly.1";
        auCl.saveAssemblyFromFasta(new SaveAssemblyParams().withAssemblyName(assemblyName).withWorkspaceName(getWsName())
                .withFile(new FastaAssemblyFile().withPath(tempFastaFile.getAbsolutePath())));
        String assemblyRef = getWsName() + "/" + assemblyName;
        Map<String, List<Tuple4 <String, Long, String, Long>>> requestedFeatures = 
                new LinkedHashMap<String, List<Tuple4 <String, Long, String, Long>>>();
        Genome genome = new UObject(new File("/kb/module/test/data/GCF_000002945/genome.json")).asClassInstance(Genome.class);
        for (Feature ft : genome.getFeatures()) {
            if (ft.getDnaSequence() == null) {
                continue;
            }
            requestedFeatures.put(ft.getId(), ft.getLocation());
        }
        System.out.println("Features: " + requestedFeatures.size());
        GetDNASequencesOutput ret = impl.getDnaSequences(new GetDNASequencesParams().withAssemblyRef(assemblyRef)
                .withRequestedFeatures(requestedFeatures), token, getContext());
        Map<String, String> dnaSequences = ret.getDnaSequences();
        for (Feature ft : genome.getFeatures()) {
            if (!requestedFeatures.containsKey(ft.getId())) {
                continue;
            }
            String origDnaSeq = ft.getDnaSequence();
            String newDnaSeq = dnaSequences.get(ft.getId());
            Assert.assertEquals("DNA is different for " + ft.getId(), origDnaSeq, newDnaSeq);
        }
    }

    @Test
    public void testGetDnaSequencesForContigSet() throws Exception {
        File fastaFile = new File("/kb/module/test/data/GCF_000002945/assembly.fa");
        String csName = "ContigSet.1";
        List<Contig> contigs = new ArrayList<Contig>();
        FastaReader fr = new FastaReader(fastaFile);
        while (true) {
            String[] entry = fr.read();
            if (entry == null)
                break;
            contigs.add(new Contig().withId(entry[0]).withSequence(entry[1]));
        }
        ContigSet csObj = new ContigSet().withContigs(contigs).withId(csName).withMd5("")
                .withSource("KBase").withSourceId("KBase");
        wsClient.saveObjects(new SaveObjectsParams().withWorkspace(getWsName()).withObjects(Arrays.asList(
                new ObjectSaveData().withType("KBaseGenomes.ContigSet").withName(csName)
                .withData(new UObject(csObj)))));
        String csRef = getWsName() + "/" + csName;
        Map<String, List<Tuple4 <String, Long, String, Long>>> requestedFeatures = 
                new LinkedHashMap<String, List<Tuple4 <String, Long, String, Long>>>();
        Genome genome = new UObject(new File("/kb/module/test/data/GCF_000002945/genome.json")).asClassInstance(Genome.class);
        for (Feature ft : genome.getFeatures()) {
            if (ft.getDnaSequence() == null) {
                continue;
            }
            requestedFeatures.put(ft.getId(), ft.getLocation());
        }
        System.out.println("Features: " + requestedFeatures.size());
        GetDNASequencesOutput ret = impl.getDnaSequences(new GetDNASequencesParams().withContigsetRef(csRef)
                .withRequestedFeatures(requestedFeatures), token, getContext());
        Map<String, String> dnaSequences = ret.getDnaSequences();
        for (Feature ft : genome.getFeatures()) {
            if (!requestedFeatures.containsKey(ft.getId())) {
                continue;
            }
            String origDnaSeq = ft.getDnaSequence().toUpperCase();
            String newDnaSeq = dnaSequences.get(ft.getId());
            Assert.assertEquals("DNA is different for " + ft.getId(), origDnaSeq, newDnaSeq);
        }
    }
}
