function switchURL(username, password)
{
    var url = window.location.host;
    url = url + window.location.pathname;
    if(url === "moodle.hku.hk/login/index.php")
        return clickUsePortalUID();
    else if(url === "hkuportal.hku.hk/cas/login")
        return enterUIDandPINandClick(username, password);
    else if(window.location.host === "moodle.hku.hk")
        return clearViewElements();
    else
    {
        clearViewElements();
        return "not a desired page";
    }
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
    var form2submit = document.getElementsByName("form")[0]
    if(form2submit!==null)
        form2submit.submit();
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

    var header = document.getElementById("page-header");
    if (header!== null)
        header.style.display='none';

    var navbar = document.getElementById("page-navbar");
    if(navbar!==null)
        navbar.style.display='none';

    var progress = document.getElementById("completionprogressid");
    if(progress!==null)
        progress.style.display='none';

    var dock = document.getElementById("dock");
    if(dock!==null)
        dock.style.display='none';

    var page = document.getElementById("page");
    if(page!==null)
        page.style.paddingLeft = "20px";

    var blocks = document.getElementsByClassName("block");
    if(blocks!==null && blocks.length!==0)
    {
        for(var each of blocks)
        {
            each.style.display = "none"
        }
    }

    return "Success at clearViewElements()";
}


