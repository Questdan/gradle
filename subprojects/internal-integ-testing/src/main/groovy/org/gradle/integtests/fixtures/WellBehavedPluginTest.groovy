/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.integtests.fixtures

import org.gradle.api.internal.plugins.DefaultPluginManager
import org.gradle.util.internal.GUtil
import org.junit.Assume

import java.util.regex.Pattern

abstract class WellBehavedPluginTest extends AbstractIntegrationSpec {

    String getPluginName() {
        def matcher = Pattern.compile("(\\w+)Plugin(GoodBehaviour)?(Integ(ration)?)?Test").matcher(getClass().simpleName)
        if (matcher.matches()) {
            return GUtil.toWords(matcher.group(1), (char) '-')
        }
        throw new UnsupportedOperationException("Cannot determine plugin id from class name '${getClass().simpleName}.")
    }

    String getQualifiedPluginId() {
        DefaultPluginManager.CORE_PLUGIN_PREFIX + getPluginName()
    }

    String getMainTask() {
        return "assemble"
    }

    @ToBeFixedForConfigurationCache(bottomSpecs = [
        "BuildDashboardPluginIntegrationTest",
        "ScalaPluginGoodBehaviourTest",
        "AntlrPluginIntegrationTest",
        "PlayApplicationPluginGoodBehaviourIntegrationTest",
        "PmdPluginIntegrationTest",
        "CppLibraryPluginIntegrationTest",
        "CppApplicationPluginIntegrationTest",
        "XcodePluginIntegrationTest",
        "IdeaPluginGoodBehaviourTest"
    ])
    def "can apply plugin unqualified"() {
        given:
        applyPluginUnqualified()

        expect:
        succeeds mainTask
    }

    def "plugin does not force creation of build dir during configuration"() {
        given:
        applyPlugin()

        when:
        run "tasks"

        then:
        !file("build").exists()
    }

    @ToBeFixedForConfigurationCache(bottomSpecs = [
        "BuildDashboardPluginIntegrationTest",
        "ScalaPluginGoodBehaviourTest",
        "AntlrPluginIntegrationTest",
        "PlayApplicationPluginGoodBehaviourIntegrationTest",
        "PmdPluginIntegrationTest",
        "CppLibraryPluginIntegrationTest",
        "CppApplicationPluginIntegrationTest",
        "XcodePluginIntegrationTest",
        "IdeaPluginGoodBehaviourTest"
    ])
    def "plugin can build with empty project"() {
        given:
        applyPlugin()

        expect:
        succeeds mainTask
    }

    protected applyPlugin(File target = buildFile) {
        target << "apply plugin: '${getQualifiedPluginId()}'\n"
    }

    protected applyPluginUnqualified(File target = buildFile) {
        target << "apply plugin: '${getPluginName()}'\n"
    }

    def "does not realize all possible tasks"() {
        // TODO: This isn't done yet, we still realize many tasks
        // Eventually, this should only realize "help"

        Assume.assumeFalse(pluginName in [
            'xctest', // Almost, still realizes compileTestSwift

            'visual-studio',
            'xcode',

            'play-application',
        ])

        applyPlugin()

        buildFile """
            tasks.configureEach {
                println("configuring \${it.path}")
            }
        """

        when:
        succeeds("help")

        then:
        def appliesBasePlugin = !(pluginName in [
            'build-dashboard', 'build-init', 'help-tasks', 'wrapper',
            'ivy-publish', 'maven-publish', 'publishing',
            'eclipse', 'idea', 'version-catalog'
        ])
        if (appliesBasePlugin) {
            assert output.count("configuring :") == 2
            outputContains("configuring :help")
            // because capturing registered outputs for stale output cleanup forces configuring clean
            outputContains("configuring :clean")
        } else {
            assert output.count("configuring :") == 1
            outputContains("configuring :help")
        }
    }

    @ToBeFixedForConfigurationCache(bottomSpecs = [
        "AntlrPluginIntegrationTest",
        "ApplicationPluginIntegrationTest",
        "AssemblerLangPluginIntegrationTest",
        "AssemblerPluginIntegrationTest",
        "BasePluginGoodBehaviourTest",
        "CLangPluginIntegrationTest",
        "CPluginIntegrationTest",
        "CUnitPluginIntegrationTest",
        "CoffeeScriptBasePluginIntegrationTest",
        "CppApplicationPluginIntegrationTest",
        "CppLangPluginIntegrationTest",
        "CppLibraryPluginIntegrationTest",
        "CppPluginIntegrationTest",
        "CppUnitTestPluginIntegrationTest",
        "DistributionPluginIntegrationTest",
        "EarPluginGoodBehaviourTest",
        "EnvJsPluginIntegrationTest",
        "GoogleTestPluginIntegrationTest",
        "GroovyPluginGoodBehaviourTest",
        "JUnitTestSuitePluginGoodBehaviourTest",
        "JavaBasePluginGoodBehaviourTest",
        "JavaGradlePluginPluginIntegrationTest",
        "JavaLanguagePluginGoodBehaviourTest",
        "JavaLibraryDistributionIntegrationTest",
        "JavaPluginGoodBehaviourTest",
        "JavaScriptBasePluginIntegrationTest",
        "JsHintPluginIntegrationTest",
        "MavenPluginGoodBehaviourTest",
        "NativeComponentPluginIntegrationTest",
        "ObjectiveCLangPluginIntegrationTest",
        "ObjectiveCPluginIntegrationTest",
        "ObjectiveCppLangPluginIntegrationTest",
        "ObjectiveCppPluginIntegrationTest",
        "PlayCoffeeScriptPluginGoodBehaviourIntegrationTest",
        "PlayJavaScriptPluginGoodBehaviourIntegrationTest",
        "RhinoPluginIntegrationTest",
        "ScalaLanguagePluginGoodBehaviourTest",
        "ScalaPluginGoodBehaviourTest",
        "SwiftApplicationPluginIntegrationTest",
        "SwiftLibraryPluginIntegrationTest",
        "WarPluginGoodBehaviourTest",
        "WindowsResourceScriptPluginIntegrationTest",
        "WindowsResourcesPluginIntegrationTest",
    ])
    def "does not realize all possible tasks if the build is included"() {
        Assume.assumeFalse(pluginName in ['xctest', 'visual-studio', 'xcode', 'play-application'])

        def includedBuildFile = file("included/build.gradle")

        settingsFile << """
            includeBuild 'included'
        """

        applyPlugin(includedBuildFile)
        includedBuildFile << """
            tasks.configureEach {
                println("configuring \${it.path}")
            }
        """

        when:
        succeeds("help")

        then:
        assert output.count("configuring :") == 0
    }
}
