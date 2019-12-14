# HKU Moodle Helper

Maybe every student graduated from the University of Hong Kong will complaint the Moodle page of this university
which can the most frequently used website during their school time. The website is powerful, though, containing 
all the learning resources and convenient entries to all kinds of e-learning online pages. However, the HKU Moodle
stores all the courses you have ever taken. Consider that after 2 or 3 years in the University, you may have already
enrolled in many a course or activity. Most of them, although you are not going to access it any longer, still appears
in the webpage together with your other courses. This make the view messy. 

One solution to get rid of it is to access the courses via HKU Portal website. But that one is not friendly to
mobile devices users at all. Can you imagine that in the 21st century, the Portal website of a Top 30 university
is not reponsive? By now, we can realize that accessing the courses resources on your mobile phone, no matter which
method to use, is annoying. 

#### Therefore, I have come up with this Android APP, enabling you to get to the courses you have enrolled in currently
#### easily and conveniently. 

## Basic Functionalities: 

Using the APP, you can 
 * add, edit or delete course cards;
 * jump to your course page directly; 
 * login automatically; 
 * download and view file directly without jumping to web browsers and login again.
 
## How to install

The APK file are included in the *RELEASE*. Go to the *RELEASE* tab and you can download it onto your mobile
phones. 

### Requirements for devices: 
1. Android Devices
1. Version no lower than Android 9.0
1. Storage permission are granted
 
## Code Structure

Open the repo in Android Studio. 

The source codes are located in `./app/src/main/java/src/main/java/cs/hku/hk//moodlehelper/`. 

There exist **TWO** packages. **activities**  contains the JAVA files for activities and 
**support** contains those for adapters, customized listeners and etc. 

Resources file are in `./app/src/main/java/src/main/res`, includes the drawables, javascripts which
interact with Moodle website and layouts. 

## Planned features
- [ ] Enable syncing with HKU Portal E-Learning. 
- [ ] Beautify the outlook and layouts. 
- [ ] Display the course names togethe with the course codes. 

-----

# Important
### 致开发者 / To developers

Thank you for your interest in this 
project. For a better workflow, I would like to
suggest the following procedures to you. 

1. 从project中选择一项，或者新建一个project项目，将其中的某个todo移至in progress
1. 在网站上创设一个新的远程branch，命名为dev_xxxx，xxxx表示新的feature或者待解决的bug
1. 在本地拉取这个分支，进行本地开发
本地开发的流程您完全可以选择您习惯的方式进行，以下仅仅是推荐：
签出一个新的本地分支，在其上进行多次提交，通过测试后，使用merge --squash或者rebase -i 将多次提交合并成为一个并合并到dev_xxxx上
1. 拉取，冲突修正和推送
1. 在以上过程重复多次后，dev_xxxx上的开发任务可以告一段落，在通过了测试后，可以发出pull request，从而将这个分支合并回master，本分支可以签出，project里面对应的in progress也可以移动到done
1. 当当前的几个dev分之都对master进行过更新之后，便可以发出release，发布下一版本

若您觉得您使用的方法或者实现，其他程序员并不能很容易地理解，烦请在Wiki中新建页面并留下解释

For those who are willing to coordinate: 
1. Pick one or create a new project in the project tab. Move one to-do to the in progress column. 
1. Create a new remote branch named dev_xxxx where xxxx refers to a new feature or a big to be solved. 
1. Fetch the new remote branch. 
For the work flow on the local dir, it is totally depends on your preference. What is offered here is just an advice. 
     - Checkout to a new local branch
     - Multiple commits there
     - Use rebase -i or merge --squash to combine the multiple commits into one and merge the one to dev_xxxx
1. pull, resolve conflicts and push. 
1. After pushing several times, if the feature is fully implemented or the bug is solved, you may issue a pull request to merge the dev_xxxx into master. The dev_xxxx can be deleted and the in-progress card shall be moved to the done column
1. After all the current dev branches have updated master with periodical achievements, a release can be created. 
1. If you feel the techniques you just implemented is a bit difficult for other programmers to catch up, you can leave explanation in Wiki by creating a new Wiki page. 
