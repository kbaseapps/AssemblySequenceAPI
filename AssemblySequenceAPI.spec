/*
A KBase module: AssemblySequenceAPI
*/

module AssemblySequenceAPI {
    typedef list<tuple<string contig_id, int start, string strand, int length>> Location;

    /*
        Reference to an Assembly object in the workspace
        @id ws KBaseGenomeAnnotations.Assembly
    */
    typedef string Assembly_ref;

    /*
        Reference to a ContigSet object containing the contigs for this genome in the workspace
        @id ws KBaseGenomes.ContigSet
    */
    typedef string ContigSet_ref;

    typedef structure {
        Assembly_ref assembly_ref;
        ContigSet_ref contigset_ref;
        mapping<string feature_id, Location> requested_features;
    } GetDNASequencesParams;

    typedef structure {
        mapping<string feature_id, string dna> dna_sequences;
    } GetDNASequencesOutput;

    funcdef get_dna_sequences(GetDNASequencesParams params)
        returns (GetDNASequencesOutput) authentication required;
};
