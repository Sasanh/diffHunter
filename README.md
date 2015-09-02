# diffHunter

mRNA stability, biogenesis and translation are greatly influenced by RNA-binding proteins (RBPs) which form mRNP complexes with their targeted mRNA. Several peak calling approaches are developed to identify protein binding sites on mRNA using short read data resulting from different cross-linking techniques. However, only few methods exist that are able to identify differential binding patterns in given pair of conditions. diffHunter is a window based peak calling approach that is not only able to identify the potential binding sites but also quantifies the differences in binding patterns among a pair of sample experiments. We employed a NoSQL approach for managing the underlying genomic read information and a concurrent mechanism for identifying the binding differences. Consequently, diffHunter is computationally efficient and a scalable software that is able to unambiguously quantify differential binding profiles between a pair of conditions and technically performs the comparison across any kind of high-throughput sequencing read samples such as CLIP and protein occupancy datasets. DiffHunter is available as both a command line software as well as a windows application that is able to quantify and visualize the differences.

##Parameters
```
$usage: diffhunter
 -1,--first <arg>       First condition
 -2,--second <arg>      Second condition
 -b,--bed <arg>         bed file to be indexed
 -c,--compare           Finding differences between two conditions
 -i,--index             Indexing BED files.
 -o,--output <arg>      Folder that the index/comparison file will be
                        created.
 -r,--reference <arg>   Reference annotation file to be used for indexing
 -s,--sliding <arg>     Length of sliding
 -w,--window <arg>      Length of window for identifying differences
```
##Input File Format
Currently, diffHunter takes <b>Sorted BED</b> files for generating the Oracle Berkeley db files. 

## Indexing a sample file
Samples are needed to be indexed before comparison: 

Example:
```
java -jar diffHunter -i -b sorted_sample.bed -r mouse_mm9.txt -o /location/home/DB_Files
```
