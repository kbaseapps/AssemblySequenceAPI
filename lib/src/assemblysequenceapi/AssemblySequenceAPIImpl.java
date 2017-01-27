package assemblysequenceapi;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import us.kbase.abstracthandle.AbstractHandleClient;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.Tuple4;
import us.kbase.common.service.UObject;
import us.kbase.shock.client.BasicShockClient;
import us.kbase.shock.client.ShockNodeId;
import us.kbase.workspace.GetObjects2Params;
import us.kbase.workspace.ObjectSpecification;
import us.kbase.workspace.WorkspaceClient;

public class AssemblySequenceAPIImpl {
    private URL wsUrl = null;
    private URL shockUrl = null;
    private URL handleUrl = null;
    
    public AssemblySequenceAPIImpl(Map<String, String> config) throws Exception {
        this.wsUrl = new URL(config.get("workspace-url"));
        this.shockUrl = new URL(config.get("shock-url"));
        this.handleUrl = new URL(config.get("handle-service-url"));
    }
    
    public GetDNASequencesOutput getDnaSequences(GetDNASequencesParams params, 
            AuthToken token) throws Exception {
        WorkspaceClient wsCl = new WorkspaceClient(this.wsUrl, token);
        wsCl.setIsInsecureHttpConnectionAllowed(true);
        String ref = params.getAssemblyRef();
        boolean isLegacy = false;
        if (ref == null) {
            ref = params.getContigsetRef();
            isLegacy = true;
        }
        UObject assemblyData = wsCl.getObjects2(new GetObjects2Params().withObjects(Arrays.asList(
                new ObjectSpecification().withRef(ref)))).getData().get(0).getData();
        ContigConsumer contigConsumer = getContigConsumer(params.getRequestedFeatures());
        String shockNodeId = null;
        if (isLegacy) {
            Map<String, Object> contigSet = assemblyData.asClassInstance(
                    new TypeReference<Map<String, Object>>() {});
            boolean lostSequence = false;
            if (contigSet.get("contigs") == null) {
                lostSequence = true;
            } else {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> contigs = (List<Map<String, Object>>)contigSet.get(
                        "contigs");
                for (Map<String, Object> contig : contigs) {
                    String sequence = (String)contig.get("sequence");
                    if (sequence == null) {
                        lostSequence = true;
                        break;
                    }
                }
                if (!lostSequence) {
                    for (Map<String, Object> contig : contigs) {
                        String contigId = (String)contig.get("id");
                        String sequence = (String)contig.get("sequence");
                        contigConsumer.processContig(contigId, sequence);
                    }
                }
            }
            if (lostSequence) {
                shockNodeId = (String)contigSet.get("fasta_ref");
            }
        } else {
            Map<String, Object> assembly = assemblyData.asClassInstance(
                    new TypeReference<Map<String, Object>>() {});
            String handleId = (String)assembly.get("fasta_handle_ref");
            AbstractHandleClient handleClient = new AbstractHandleClient(handleUrl, token);
            shockNodeId = handleClient.hidsToHandles(Arrays.asList(handleId)).get(0).getId();
            BasicShockClient shockCl = new BasicShockClient(shockUrl, token);
            FastaReader fr = new FastaReader(new InputStreamReader(shockCl.getFile(
                    new ShockNodeId(shockNodeId))));
            while (true) {
                String[] entry = fr.read();
                if (entry == null) {
                    break;
                }
                contigConsumer.processContig(entry[0], entry[1]);
            }
            fr.close();
        }
        Map<String, String> dnaSequences = contigConsumer.finalizeDnaSequences();
        return new GetDNASequencesOutput().withDnaSequences(dnaSequences);
    }
    
    private ContigConsumer getContigConsumer(
            final Map<String, List<Tuple4<String, Long, String, Long>>> requestedFeatures) {
        final Map<String, String> ret = new LinkedHashMap<String, String>();
        return new ContigConsumer() {

            @Override
            public void processContig(String contigId, String sequence)
                    throws Exception {
                for (String featureId : requestedFeatures.keySet()) {
                    if (ret.containsKey(featureId)) {
                        continue;
                    }
                    List<Tuple4<String, Long, String, Long>> location = 
                            requestedFeatures.get(featureId);
                    if (location.size() > 0 && location.get(0).getE1().equals(contigId)) {
                        ret.put(featureId, extractFeatureSequence(sequence, featureId, location));
                    }
                }
            }
            
            @Override
            public Map<String, String> finalizeDnaSequences() throws Exception {
                return ret;
            }
        };
    }
    
    private static String extractFeatureSequence(String contigSequence, String featureId,
            List<Tuple4<String, Long, String, Long>> location) {
        boolean fwd = location.get(0).getE3().equals("+");
        StringBuilder ret = new StringBuilder();
        for (Tuple4<String, Long, String, Long> loc : location) {
            // Both start and end are 1-based and end is included
            long start = fwd ? loc.getE2() : (loc.getE2() - (loc.getE4() - 1));
            long end = fwd ? (loc.getE2() + (loc.getE4() - 1)) : loc.getE2();
            if (start < 0 || end < 0) {
                throw new IllegalStateException("Feature " + featureId + " has wrong " +
                		"location: " + location.toString());
            }
            String part = contigSequence.substring((int)start - 1, (int)end).toUpperCase();
            if (!fwd) {
                char[] inChars = part.toCharArray();
                char[] outChars = new char[inChars.length];
                for (int i = 0; i < outChars.length; i++) {
                    char inCh = inChars[inChars.length - 1 - i];
                    char outCh = 'N';
                    switch (inCh) {
                    case 'A':
                        outCh = 'T';
                        break;
                    case 'T':
                        outCh = 'A';
                        break;
                    case 'G':
                        outCh = 'C';
                        break;
                    case 'C':
                        outCh = 'G';
                        break;
                    }
                    outChars[i] = outCh;
                }
                part = new String(outChars);
            }
            ret.append(part);
        }
        return ret.toString();
    }
    
    public static interface ContigConsumer {
        public void processContig(String contigId, String sequence) throws Exception;
        public Map<String, String> finalizeDnaSequences() throws Exception;
    }
}
