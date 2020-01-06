function switchURL(username, password)
{
    var returnStr = "";
    if(window.location.host==="sis-eportal.hku.hk" && window.location.search==="?tab=DEFAULT")
        returnStr += jumpToELearning();
    else if(window.location.host==="hkuportal.hku.hk" && window.location.pathname==="/login.html")
        returnStr += loginToPortal(username, password);
    else
        returnStr += jumpToELearning();

    return returnStr;
}

function retrieveCourses()
{
    var courseTable = document.getElementById("courses").children[0];
    if(courseTable===null) return null;
    var courseList = [];
    for(i of courseTable.children)
    {
        if(i.getAttribute('index')!==null)
        {
            courseList.push({'course_name':i.firstElementChild.innerHTML,
                             'course_url':i.lastElementChild.getElementsByTagName('a')[0].getAttribute('href'),
                             'course_title':i.children[i.childElementCount-2].innerHTML});
        }
    }
    return JSON.stringify(courseList);
}

function jumpToELearning()
{
    var hyperLink = document.getElementsByClassName('pthomepagetabline')[0].children[0].firstElementChild.children[5].getElementsByTagName('a')[0];
    if(hyperLink !== null)
    {
        hyperLink.click();
        return "Success are jumpToELearning";
    }
    return "Fail are jumpToELearning";
}

function loginToPortal(username, password)
{
    var userNameField = document.getElementById("username");
    var passwordField = document.getElementById("password");
    if(userNameField!==null && username!==null && username!=="")
        userNameField.value = username;
    if(passwordField!==null && password!==null && password!=="")
        passwordField.value = password;
    var form2submit = document.getElementsByName("form")[0]
    if(form2submit!==null)
        form2submit.submit();
    return "Success are enterUIDandPINandClick(\""+username+"\",\""+password+"\")";
}

