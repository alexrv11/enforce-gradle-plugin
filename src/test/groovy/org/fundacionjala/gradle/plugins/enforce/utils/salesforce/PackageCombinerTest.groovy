package org.fundacionjala.gradle.plugins.enforce.utils.salesforce

import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths

class PackageCombinerTest extends Specification {

    @Shared
        def SRC_PATH = Paths.get(System.getProperty("user.dir"), "src", "test", "groovy", "org",
                "fundacionjala", "gradle", "plugins","enforce","utils", "salesforce").toString()
    @Shared
        PackageCombiner packageCombiner

    def setup() {
        packageCombiner = new PackageCombiner()
    }

    def 'Test should combine two packages from project directory package and build directory package'() {
        given:
            String projectPackagePath = Paths.get(SRC_PATH, 'projectPackage.xml')
            String buildPackagePath = Paths.get(SRC_PATH, 'buildPackage.xml')
            String projectPackageContent = '''<?xml version='1.0' encoding='UTF-8'?>
                    <Package xmlns='http://soap.sforce.com/2006/04/metadata'>
                        <types>
                            <members>Object1__c.Field1</members>
                            <members>Object2__c.Field2</members>
                            <name>CustomField</name>
                        </types>
                        <version>32.0</version>
                    </Package>
                    '''
            String buildPackageContent ='''<?xml version='1.0' encoding='UTF-8'?>
                    <Package xmlns='http://soap.sforce.com/2006/04/metadata'>
                        <types>
                            <members>Object1__c</members>
                            <name>CustomObject</name>
                        </types>
                    <version>32.0</version></Package>
                    '''
            File projectPackageFile = new File(projectPackagePath)
            projectPackageFile.write(projectPackageContent)
            File buildPackageFile = new File(buildPackagePath)
            buildPackageFile.write(buildPackageContent)
        when:
            PackageCombiner.packageCombine(projectPackagePath, buildPackagePath)
            XMLUnit.ignoreWhitespace = true
            def xmlDiff = new Diff(buildPackageFile.text, projectPackageContent)
        then:
            new File(Paths.get(SRC_PATH, 'buildPackage.xml').toString()).exists()
            xmlDiff.similar()
    }

    def 'Test should combine two packages from project directory package and build directory package without *'() {
        given:
            String projectPackagePath = Paths.get(SRC_PATH, 'projectPackage.xml')
            String buildPackagePath = Paths.get(SRC_PATH, 'buildPackage.xml')
            String projectPackageContent = '''<?xml version='1.0' encoding='UTF-8'?>
                        <Package xmlns='http://soap.sforce.com/2006/04/metadata'>
                            <types>
                                <members>Object1__c.Field1</members>
                                <members>Object2__c.Field2</members>
                                <name>CustomField</name>
                            </types>
                            <types>
                                <members>*</members>
                                <name>ApexClass</name>
                            </types>
                            <version>32.0</version>
                        </Package>
                        '''
            String buildPackageContent ='''<?xml version='1.0' encoding='UTF-8'?>
                        <Package xmlns='http://soap.sforce.com/2006/04/metadata'>
                            <types>
                                <members>Object1__c</members>
                                <name>CustomObject</name>
                            </types>
                            <types>
                                <members>Class1</members>
                                <name>ApexClass</name>
                            </types>
                        <version>32.0</version></Package>
                        '''
            File projectPackageFile = new File(projectPackagePath)
            projectPackageFile.write(projectPackageContent)
            File buildPackageFile = new File(buildPackagePath)
            buildPackageFile.write(buildPackageContent)
            String packageContentExpect = '''<?xml version='1.0' encoding='UTF-8'?>
                        <Package xmlns='http://soap.sforce.com/2006/04/metadata'>
                            <types>
                                <members>Class1</members>
                                <name>ApexClass</name>
                            </types>
                            <types>
                                <members>Object1__c.Field1</members>
                                <members>Object2__c.Field2</members>
                                <name>CustomField</name>
                            </types>
                            <version>32.0</version>
                        </Package>
                        '''
        when:
            PackageCombiner.packageCombine(projectPackagePath, buildPackagePath)
            XMLUnit.ignoreWhitespace = true
            def xmlDiff = new Diff(buildPackageFile.text, packageContentExpect)
        then:
            new File(Paths.get(SRC_PATH, 'buildPackage.xml').toString()).exists()
            xmlDiff.similar()
    }

    def cleanupSpec() {
        new File(Paths.get(SRC_PATH, 'projectPackage.xml').toString()).delete()
        new File(Paths.get(SRC_PATH, 'buildPackage.xml').toString()).delete()
    }
}