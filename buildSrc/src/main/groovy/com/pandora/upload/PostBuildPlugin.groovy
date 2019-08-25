package com.pandora.upload

import org.gradle.api.Plugin
import org.gradle.api.Project

class PostBuildPlugin implements Plugin<Project> {

    //文件备份路径
    //def backFileRootDir = Util.sIsTest ? "/Users/dingbaosheng/shared/" : "/home/qwe/shared/"
    def backFileRootDir = Util.sIsTest ? "../upload/APK/" : "../upload/APK/"

    @Override
    void apply(Project project) {
        println "在这里进行apk打包上传发邮件操作..." + project.name

        //遍历正在执行的task任务
        project.tasks.whenTaskAdded { task ->
            println "task name: --->> ${task.getName()}"
            if (task.getName() == "assembleDebug") {
                println "assembleDebug task: --->> ${task.getName()}"
                task.doLast {
                    println "assembleDebug task: --->> start..."
                    //jenkins配置
                    def jenkinsJobName = System.getenv('JOB_NAME') ?: "local_job"
                    def jenkinsBuild = System.getenv('BUILD_NUMBER') ?: "0"

                    //config.json配置
                    def vName = project.getProperties().get('vName', project.rootProject.ext.config.versionName)
                    def shouldUpload = project.getProperties().get('shouldUpload', false)

                    //判断是否需要执行拷贝文件，如果不是jenkins上build直接返回
                    if (!Util.sIsTest) {
                        if (jenkinsJobName == 'local_job') {
                            //do nothing
                            return
                        }
                    }

                    //上传到jenkins服务器的地址
                    def fileBackPath = backFileRootDir + project.rootProject.ext.config.upload.jenkinsFileBackDir + File.separator + jenkinsJobName + File.separator + jenkinsBuild

                    println("==========jenkinsJobName: " + jenkinsJobName + "===========")
                    println("==========jenkinsBuild: " + jenkinsBuild + "===========")
                    println("==========vName: " + vName + "===========")
                    println("==========shouldUpload: " + shouldUpload + "===========")
                    println("==========fileBackPath: " + fileBackPath + "===========")

                    println("==========start copy  file start===========")

                    //拷贝apk
                    project.copy {
                        from('build/outputs')
                        into(fileBackPath + '/outputs')
                    }
//
                    println("==========end copy file end===========")

                }
            } else if (task.getName() == "assembleRelease") {
                println "assembleRelease task: --->> ${task.getName()}"
                task.doLast {
                    println("=====assembleRelease.doLast start ======")

                    //jenkins配置
                    def jenkinsJobName = System.getenv('JOB_NAME') ?: "local_job"
                    def jenkinsBuild = System.getenv('BUILD_NUMBER') ?: "0"

                    //config.json配置
                    def vName = project.getProperties().get('vName', project.rootProject.ext.config.versionName)
                    def shouldUpload = project.getProperties().get('shouldUpload', false)

                    //判断是否需要执行拷贝文件，如果不是jenkins上build直接返回
                    if (!Util.sIsTest) {
                        if (jenkinsJobName == 'local_job') {
                            //do nothing
                            return
                        }
                    }

                    //上传到jenkins服务器的地址
                    def fileBackPath = backFileRootDir + project.rootProject.ext.config.upload.jenkinsFileBackDir + File.separator + jenkinsJobName + File.separator + jenkinsBuild

                    println("==========jenkinsJobName: " + jenkinsJobName + "===========")
                    println("==========jenkinsBuild: " + jenkinsBuild + "===========")
                    println("==========vName: " + vName + "===========")
                    println("==========shouldUpload: " + shouldUpload + "===========")
                    println("==========fileBackPath: " + fileBackPath + "===========")

                    //拷贝文件
                    println("==========begain copy file===========")
                    project.copy {
                        from('build/outputs')
                        into(fileBackPath + '/outputs')
                    }
                    println("==========end copy file===========")


                    println("shouldUpload: " + shouldUpload)
                    if (shouldUpload == "true") {
                        println('==========begin execute upload file task==========')
                        try {
                            Util.upload(project.rootDir)
                        } catch (Exception e) {
                            println("upload fails : " + e.getMessage())
                        }

                    } else {
                        println('==========no need upload file==========')
                    }
                }
            }

        }
    }
}