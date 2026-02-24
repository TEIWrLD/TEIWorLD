# TEIWorLD (TEI Workflow for Language Data)
Tool to convert several written and spoken language data formats into TEI<br>
## Description
TEIWorLD transforms a variety of different formats for spoken and written language into the standardised formats TEISpoken and I5 with the intermediate format TEI P5. For archiving written data, the pipeline converts TEI P5 to the format used at IDS, the I5 format, which was developed by IDS based on TEI P5.<br>
<div align="center">
  <img src="images/Grafik_UML.png" alt="Schematic representation of the components of TEIWorLD">
</div>

## Usage
#### Command **spoken**:<br>
Converts to TEIspoken and keeps files separate if there is more than one in the input directory<br>
`de.ids.TeiWorld spoken path\to\input\dir\ path\to\output\dir\`
```
path\to\input\dir\
├── 01.10.07-1_2_transcription.eaf       // After conversion: 01.10.07-1_2_transcription.tei_corpo.xml
├── 011017.cha                           // After conversion: 011017.tei_corpo.xml
├── 3wCno_4b_In.qdpx                     // File name will be different after conversion as only part of the qdpx archive (the transcription) is subject to conversion
├── Max_Mustermann.TextGrid              // After conversion: Max_Mustermann.tei_corpo.xml
├── B060115prs15.trs                     // After conversion: B060115prs15.tei_corpo.xml
├── Notes.docx                           // FILE WILL BE IGNORED as DOCX is no valid input format for mode spoken
├── paper_publication.pdf                // FILE WILL BE IGNORED as PDF is no valid input format for mode spoken
├── RDMO.PNG                             // FILE WILL BE IGNORED as PNG is no valid input format for mode spoken
├── emptyDirectory                       // DIRECTORY WILL BE IGNORED, only files are processed in mode spoken
├── output_9382718e-ad60ce40f155_2.json  // FILE WILL BE IGNORED as JSON is no valid input format for mode spoken
└── token-alignment.txt                  // FILE WILL BE IGNORED as TXT is no valid input format for mode spoken
```

#### Command **writtenP5**:<br>
Converts to TEI P5 and keeps the resulting files separate if there is more than one in the input directory<br>
`de.ids.TeiWorld writtenP5 path\to\input\dir\ path\to\output\dir\`
```
path\to\input\dir\
├── fileA.txt                            // After conversion: fileA.tei_garage.xml
├── fileB.txt                            // After conversion: fileB.tei_garage.xml
├── fileC.cha                            // FILE WILL BE IGNORED as CHA is no valid input format for mode writtenP5
└── fileD.docx                           // After conversion: fileD.tei_garage.xml
```

#### Command **written**:<br>
Converts to TEI I5 and combines files to a single I5 corpus. The file `metadata.json` needs to be in the same directory.<br>
The corpusSigle is taken from `metadata.json`.<br>
All files will be put under the one single dokumentSigle whose label is also extracted from `metadata.json`.
`de.ids.TeiWorld written path\to\input\dir\ path\to\output\dir\`
```
path\to\input\dir\
├── pic.PNG                              // FILE WILL BE IGNORED as PNG is no valid input format for mode written
├── file01.docx                          // 
├── file02.txt                           // 
└── metadata.json                        // MANDATORY file with the corpus metadata
```

```xml
<idsCorpus version="1.0">
    <idsHeader type="corpus" pattern="allesaußerZtg/Zschr" version="1.0"> <!-- contains metadata from meatadata.json -->
    <idsDoc type="text" version="1.0"> <!-- dokumentSigle: NOZ/DOK -->
      <idsHeader type="document" pattern="text" version="1.0">
      <idsText version="1.0">          <!-- textSigle: NOZ/DOK.00001 | empty t.title -->
	  <idsText version="1.0">          <!-- textSigle: NOZ/DOK.00002 | empty t.title -->
	</idsDoc>
</idsCorpus>
```

#### Command **writtenHierarchical**:<br>
Converts to TEI I5 and constructs the hierarchical document and text structure of a written corpus. 
The directory needs to contain the file `metadata.json` and one or more subdirectories (= idsDoc) that contain the individual texts (= idsText).<br>
In this mode only the corpusSigle is taken from `metadata.json`.
`de.ids.TeiWorld writtenHierarchical path\to\input\dir\ path\to\output\dir\`

The **folder structure of the input directory** will be reflected in the resulting **I5 XML tree**:
```
path\to\input\dir\
├── Directory01                          // After conversion: directory name = dokumentSigle 
│   ├── Kriterien für Datenaufnahme.txt  // After conversion: file name = t.title | textSigle: NOZ/Directory01.00001
│   └── Protokoll Projekttreffen.docx    // After conversion: file name = t.title | textSigle: NOZ/Directory01.00002
├── Directory02                          // After conversion: directory name = dokumentSigle
│   └── Planung Publikation.docx         // After conversion: file name = t.title | textSigle: NOZ/Directory02.00001
├── Directory03                          // After conversion: directory name = dokumentSigle
│   ├── Briefsammlung.txt                // After conversion: file name = t.title | textSigle: NOZ/Directory03.00001
│   ├── Essay.txt                        // After conversion: file name = t.title | textSigle: NOZ/Directory03.00002
│   ├── Essay_Kommentare.pdf             // FILE WILL BE IGNORED as PDF is no valid input format
│   └── Workshop Korpusaufbau.docx       // After conversion: file name = t.title | textSigle: NOZ/Directory03.00003
├── Directory04                          // DIRECTORY WILL BE IGNORED as there is no file as a direct child
│   └── folderXY                         // DIRECTORY WILL BE IGNORED, only files would be processed
│       ├── dummyFile02.txt              // FILE WILL BE IGNORED
│       └── emptyFolder                  // FILE WILL BE IGNORED
├── dummyFile01.txt                      // FILE WILL BE IGNORED as it is not inside a directory
└── metadata.json                        // MANDATORY file with the corpus metadata
```

```xml
<idsCorpus version="1.0">
    <idsHeader type="corpus" pattern="allesaußerZtg/Zschr" version="1.0"> <!-- contains metadata from meatadata.json -->
    <idsDoc type="text" version="1.0"> <!-- dokumentSigle: NOZ/Directory01 -->
      <idsHeader type="document" pattern="text" version="1.0">
      <idsText version="1.0">          <!-- textSigle: NOZ/Directory01.00001 | t.title: Kriterien für Datenaufnahme -->
	  <idsText version="1.0">          <!-- textSigle: NOZ/Directory01.00002 | t.title: Protokoll Projekttreffen -->
	</idsDoc>
	<idsDoc type="text" version="1.0"> <!-- dokumentSigle: NOZ/Directory02 -->
      <idsHeader type="text" pattern="text" version="1.0">
      <idsText version="1.0">          <!-- textSigle: NOZ/Directory02.00001 | t.title: Planung Publikation -->
	</idsDoc>
	<idsDoc type="text" version="1.0"> <!-- dokumentSigle: NOZ/Directory03 -->
      <idsHeader type="text" pattern="text" version="1.0">
	  <idsText version="1.0">          <!-- textSigle: NOZ/Directory03.00001 | t.title: Briefsammlung -->
	  <idsText version="1.0">          <!-- textSigle: NOZ/Directory03.00002 | t.title: Essay -->
	  <idsText version="1.0">          <!-- textSigle: NOZ/Directory03.00003 | t.title: Workshop Korpusaufbau -->
	</idsDoc>
</idsCorpus>
```


### Components
[TEIGarage](https://github.com/TEIC/TEIGarage)<br>
[TEICORPO](https://github.com/christopheparisse/teicorpo)<br>
P5ToI5<br>
### Data formats
#### Input (spoken formats)
eaf (Elan)<br>
textgrid (Praat)<br>
cha (chat/childes)<br>
trs (transcriber)<br>
maxqda (qdpx/mx24)<br>
#### Input (written formats)
txt<br>
docx/doc<br>
#### Output
ISO/TEI Transcriptions of Spoken Language (TEISpoken)<br>
IDS TEI P5 (I5)<br>

## Publications

## Team

## Contact
* [E-Mail](mailto:data-steward@ids-mannheim.de)

<hr style="border: 1px solid #ccc; margin-bottom: 20px;">
<footer>
  <div style="display: flex; justify-content: space-between; align-items: center;">
    <img src="images/IDS_Vorlage.svg" alt="Logo 1" style="width: 300px; height: 100px;">
    <img src="images/textplus_logo_RGB.png" alt="Logo 2" style="width: 100px; height: 100px;">
  </div>
  <p>&copy; 2025 TEIWorLD</p>
</footer>
