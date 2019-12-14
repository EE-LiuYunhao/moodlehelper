function switchURL(username, password)
{
    var url = window.location.host;
    url = url + window.location.pathname;
    if(url === "moodle.hku.hk/login/index.php")
        return clickUsePortalUID();
    else if(url === "hkuportal.hku.hk/cas/login")
        return enterUIDandPINandClick(username, password);
    else
        return clearViewElements();
}

function clickUsePortalUID()
{
    var hyperLinks = document.getElementsByTagName('a');
    for(var eachLink of hyperLinks)
    {
        if(eachLink.getAttribute('href') === "https://moodle.hku.hk/login/index.php?authCAS=CAS")
        {
            eachLink.click();
        }
    }
    return "Success at clickUsePortalUID()";
}

function enterUIDandPINandClick(username, password)
{
    var userNameField = document.getElementById("username");
    var passwordField = document.getElementById("password");
    if(userNameField!==null && username!==null && username!=="")
        userNameField.value = username;
    if(passwordField!==null && password!==null && password!=="")
        passwordField.value = password;
    document.getElementsByName("form")[0].submit();
    return "Success are enterUIDandPINandClick(\""+username+"\",\""+password+"\")";
}

function clearViewElements()
{
    var nav_blocks = document.getElementsByClassName("block_navigation");
    for (var eachBlock of nav_blocks)
    {
        eachBlock.style.display='none';
    }

    var homeHeader = document.getElementsByClassName("course-home banner");
    for (var eachHeader of homeHeader)
    {
        eachHeader.style.display='none';
    }

    document.getElementById("page-header").style.display='none';

    document.getElementById("page-navbar").style.display='none';

    document.getElementById("completionprogressid").style.display='none';

    document.getElementById("dock").style.display='none';
    return "Success at clearViewElements()";
}


