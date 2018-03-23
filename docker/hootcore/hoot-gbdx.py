# Run a Hootenanny translation using the GbdxTaskInterface

import os
import subprocess
import shutil
import zipfile

from gbdx_task_interface import GbdxTaskInterface

class HootGbdxTask(GbdxTaskInterface):

    # Nice function from StackOverflow to increment a filename if it exists
    def checkFile(self,path):
        path = os.path.expanduser(path)
        if not os.path.exists(path):
            return path

        root, ext = os.path.splitext(os.path.expanduser(path))
        dir       = os.path.dirname(root)
        fname     = os.path.basename(root)
        candidate = fname+ext
        index     = 0
        ls        = set(os.listdir(dir))
        while candidate in ls:
            candidate = "{}_{}{}".format(fname,index,ext)
            index    += 1

        return os.path.join(dir,candidate)
        # End checkFile


    # This is a bit overkill. It will search for multiple extensions but we really
    # only need one in this script
    def findFiles(self,inputDir,extens):
        found = {x: [] for x in extens} # lists of found files

        # Walk the tree
        for dirpath, dirnames, files in os.walk(inputDir):
            # Loop through the file names for the current step
            for name in files:
                # Split the name by '.' & get the last element
                ext = name.lower().rsplit('.', 1)[-1]

                # Save the full name if ext matches
                if ext in extens:
                    found[ext].append(os.path.join(dirpath, name))

        return found
        # End findFiles



    def convertFileJson(self,inputFile,outputFile):
        hootCmd = ['hoot','convert','--error',
                '-D','convert.ops=hoot::TranslationOp',
                '-D','json.add.bbox=true',
                '-D','osm.map.writer.skip.empty.map=true',
                '-D','translation.script=/var/lib/hootenanny/translations/GBDX.js']

        hootCmd.append(inputFile)
        hootCmd.append(outputFile)

        hootOut = ''

        # Debug
        # print('Hoot: %s') % (' '.join(hootCmd))

        # Very basic error checking
        try:
            hootOut = subprocess.check_output(hootCmd)

        # Question: If one file fails, should we about the lot or just keep going?
        except subprocess.CalledProcessError, e:
            return e.output

        # with "--error" the Hoot command shoud not have any output. If it does then
        # it is an error.
        if len(hootOut) > 0:
            return hootOut
        # End convertFileJson


    def convertFileXml(self,inputFile,outputFile):
        hootCmd = ['hoot','convert','--error',
                '-D','convert.ops=hoot::TranslationOp',
                '-D','json.add.bbox=true',
                '-D','osm.map.writer.skip.empty.map=true',
                '-D','translation.script=/var/lib/hootenanny/translations/GBDX_XML.js']

        hootCmd.append(inputFile)
        hootCmd.append(outputFile)

        hootOut = ''

        # Debug
        # print('Hoot: %s') % (' '.join(hootCmd))

        # Very basic error checking
        try:
            hootOut = subprocess.check_output(hootCmd)

        # Question: If one file fails, should we about the lot or just keep going?
        except subprocess.CalledProcessError, e:
            return e.output

        # with "--error" the Hoot command shoud not have any output. If it does then
        # it is an error.
        if len(hootOut) > 0:
            return hootOut
        # End convertFileXml


    def processGeoJsonFiles(self,inputDir,outputDirJson,outputDirXml):
        fList = self.findFiles(inputDir,['geojson'])

        #print 'Processing GeoJSON: Start'
        #print 'GeoJSON List: {}\n'.format(fList)

        for inputFile in fList['geojson']:

            #print 'Processing: {}'.format(inputFile)

            fName = os.path.basename(inputFile)

            # outputFile = self.checkFile(os.path.join(outputDir,fName))
            # returnText = self.convertFile(inputFile,outputFile)
            returnTextJson = self.convertFileJson(inputFile,outputDirJson)
            if returnTextJson is not None:
                self.status = 'failed'
                self.reason = returnTextJson

            returnTextXml = self.convertFileXml(inputFile,outputDirXml)
            if returnTextXml is not None:
                self.status = 'failed'
                self.reason = returnTextJson + ';' + returnTextXml
        #print 'Processing GeoJSON: End'
        # End processGeoJsonFiles


    def processJsonFiles(self,inputDir,outputDirJson,outputDirXml):
        tmpInName = self.checkFile(os.path.join(inputDir,'hootIn%s.geojson' % os.getpid()))
        # tmpOutName = self.checkFile(os.path.join(outputDir,'hootOut%s.geojson' % os.getpid()))
        #tmpOutName = self.checkFile(os.path.join(outputDir,'hootOut%s.geojson' % os.getpid()))
        fList = self.findFiles(inputDir,['json'])

        # print 'Processing JSON: Start'
        # print('JSON List: %s\n' % fList)

        for inputFile in fList['json']:

            #print 'Processing: {}'.format(inputFile)

            # Make sure that we don't have anything from a previous run
            if os.path.isfile(tmpInName):
                os.remove(tmpInName)

            # if os.path.isfile(tmpOutName):
            #     os.remove(tmpOutName)

            # Now rename the input to the tmp Input geojson name
            os.rename(inputFile,tmpInName)

            # Process the file and output it to the tmp Output geojson name
            # returnText = self.convertFile(tmpInName,tmpOutName)
            returnTextJson = self.convertFileJson(tmpInName,outputDirJson)
            if returnTextJson is not None:
                self.status = 'failed'
                self.reason = returnTextJson

            returnTextXml = self.convertFileXml(tmpInName,outputDirXml)
            if returnTextXml is not None:
                self.status = 'failed'
                self.reason = returnTextJson + ';' + returnTextXml

            # Make sure that we got some output before trying to do anything with it.
            # if os.path.isfile(tmpOutName):
            #     fName = os.path.basename(inputFile)
            #     outputFile = self.checkFile(os.path.join(outputDir,fName))
            #     os.rename(tmpOutName,outputFile)
        
            # Clean up. Move the input file back to what it was
            os.rename(tmpInName,inputFile)

        # print 'Processing JSON: End'
        # End processJsonFiles


    def processZipFiles(self,inputDir,outputDirJson,outputDirXml):
        tmpDirName = self.checkFile(os.path.join(inputDir,'hoot%s' % os.getpid()))
        fList = self.findFiles(inputDir,['zip'])
        # print '\nProcessing Zip: Start'
        # print '  Zip inputDir: {}'.format(inputDir)
        # print '  Zip TmpDir: {}'.format(tmpDirName)
        # print '  Zip List: {}\n'.format(fList)

        for inputFile in fList['zip']:
            # print 'Processing Zip File: {}'.format(inputFile)
    
            # Defensive cleaning 
            if os.path.isfile(tmpDirName):
                os.remove(tmpDirName)

            if os.path.isdir(tmpDirName):
                shutil.rmtree(tmpDirName)

            # print '    About to Create: {}'.format(tmpDirName)
            os.makedirs(tmpDirName)

            try:
                zip = zipfile.ZipFile(inputFile)
                zip.extractall(path=tmpDirName)

            except zipfile.BadZipFile, e:
                # Just rm the Dir and move on
                shutil.rmtree(tmpDirName)
                continue

            # Make sure that we have files to work with
            tList = self.findFiles(tmpDirName,['geojson','json','zip'])

            #print 'Unpacked Zip: {}  Files: {}\n'.format(inputFile,tList)

            # Check if the zip file has anything we can process
            if tList['geojson'] == [] and tList['json'] == [] and tList['zip'] == []:
                # print 'Empty Zip Directory'
                # print 'About to Remove: {}\n'.format(tmpDirName)
                shutil.rmtree(tmpDirName)
                continue

            # Now process the unpacked files
            self.processGeoJsonFiles(tmpDirName,outputDirJson,outputDirXml)
            self.processJsonFiles(tmpDirName,outputDirJson,outputDirXml)

            # Now get recursive :-)  We may have zip files of zip files
            # This is very defensive
            # print '## About to Process Zip Files'
            self.processZipFiles(tmpDirName,outputDirJson,outputDirXml)
            # print '## Back from Process Zip Files'
        
            # Cleanup before returning
            # print '    About to Remove: {}\n'.format(tmpDirName)
            shutil.rmtree(tmpDirName)

        # print 'Processing Zip: End'
        # End processZipFiles


    def invoke(self):
        inputDir = self.get_input_data_port('geojson')

        # outputDir = os.path.join(self.get_output_data_port('data_out'),'results')
        outputDir = self.get_output_data_port('data_out')

        if not os.path.exists(outputDir):
            os.makedirs(outputDir)
        else:
            shutil.rmtree(outputDir)
            os.makedirs(outputDir)


        # Working Directory. We zip this and make the output.
        workDirJson = self.checkFile(os.path.join('/tmp','hootJson%s' % os.getpid()))
        workDirXml = self.checkFile(os.path.join('/tmp','hootXml%s' % os.getpid()))
        
        # print 'workDir: {}\n'.format(workDir)
        if not os.path.exists(workDirJson):
            os.makedirs(workDirJson)
        else:
            shutil.rmtree(workDirJson)
            os.makedirs(workDirJson)

        # print 'workDir: {}\n'.format(workDir)
        if not os.path.exists(workDirXml):
            os.makedirs(workDirXml)
        else:
            shutil.rmtree(WorkDirXml)
            os.makedirs(workDirXml)

        # We tell Hoot that the output is /xxx/xxx/yyy.gbdx
        # Hoot will try to mkdir /xxx/xxx/yyy and write a stack of GeoJSON files into
        # it.   
        hootWorkDirJson = workDirJson + '.gbdx'
        hootWorkDirXml = workDirXml + '.gxml'

        # Check if we have something to work with
        tList = self.findFiles(inputDir,['geojson','json','zip'])

        # Now go looking for files to convert
        self.processGeoJsonFiles(inputDir,hootWorkDirJson,hootWorkDirXml)
        self.processJsonFiles(inputDir,hootWorkDirJson,hootWorkDirXml)
        self.processZipFiles(inputDir,hootWorkDirJson,hootWorkDirXml)

        tCwd = os.getcwd()

        # Zip up what we translated
        outZip = zipfile.ZipFile(os.path.join(outputDir,'results_json.zip'),'w')
        os.chdir(workDirJson)
        for fName in os.listdir(workDirJson):
            outZip.write(fName)

        outZip.close()

        outZip = zipfile.ZipFile(os.path.join(outputDir,'results_xml.zip'),'w')
        os.chdir(workDirXml)
        for fName in os.listdir(workDirXml):
            outZip.write(fName)

        outZip.close()

        os.chdir(tCwd)

        # cleanup
        shutil.rmtree(workDirJson)
        shutil.rmtree(workDirXml)


if __name__ == "__main__":
    with HootGbdxTask() as task:
        task.invoke()
