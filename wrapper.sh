set -xe


setOutput()
{
    if [ $# -ne 2 ]
    then
        echo "Key and value required on setOutput function call"
        exit 1
    fi
    echo "${2}" > "/u01/flexdeploy/application/localhost/work/71889037/9208897/internal/outputs/${1}"
}


JAVAC=/u01/java/jdk1.8.0_281/bin/javac
JAVA=/u01/java/jdk1.8.0_281/bin/java
JAR=/u01/java/jdk1.8.0_281/bin/jar
WEB_LIB=/u01/flexdeploy/apache-tomcat-flexdeploy/webapps/flexdeploy/WEB-INF/lib
LIB=/u01/flexdeploy/apache-tomcat-flexdeploy/lib
VERSION=$(date +%s)
JAR_NAME="app-${VERSION}.jar"

cd utilities
$JAVAC -cp workflow:requests:pojo:threads:$WEB_LIB/adflibFlexFndCommonCore.jar:$WEB_LIB/json-20190722.jar:$WEB_LIB/adflibFlexDeployModel.jar:$WEB_LIB/FlexDeployModel2.jar:$LIB/javax.ws.rs-api-2.0.jar:$LIB/jersey*.jar:$LIB/commons*.jar -d target workflow/BulkWorkflowPropertiesAndValues.java requests/*.java pojo/*.java threads/*.java
cd target
rm -f *.jar
$JAR cfvm $JAR_NAME ./MANIFEST.FM workflow/BulkWorkflowPropertiesAndValues.class requests/*.class pojo/*.class threads/*.class
$JAVA -jar $JAR_NAME $BASE_URL $USERNAME $PASSWORD "$WORKFLOW_NAME" $TARGET_GROUP_CODE "$CSV_FILE_PATH" "$WORKFLOW_SOURCE"
