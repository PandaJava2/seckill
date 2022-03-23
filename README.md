# Java秒杀系统API
# How to play
git clone https://github.com/codingXiaxw/seckill.git

open IDEA --> File --> New --> Open

choose seckill's pom.xml，open it

update the jdbc.properties files about your mysql's username and password

deploy the tomcat，and start up

enter in the browser: http://localhost:8080/seckill/list

enjoy it

# Develop environment
IDEA+Maven+SSM

# Written in front of words
  
之前学习过spring、mybatis、spring mvc但学校注重科研也没有机会参与什么开发项目，所以一直没有对这三个技术进行整合，现在决定做个maven + smm框架的项目练练手，从项目中成长。
maven的强大之处就是你不用再像以前那样，如果在项目中用到spring框架还要到spring官网上去下载一系列的jar包，用了maven对项目进行管理之后你就可以直接在它的pom.xml文件中添加jar包的相应坐标，这样maven就能自动从它的中央仓库中为我们将这些jar包下载到其本地仓库中供我们使用。

完成这个秒杀系统，需要完成四个模块的代码编写，分别是:

1.Java高并发秒杀APi之业务分析与DAO层代码编写。

2.Java高并发秒杀APi之Service层代码编写。

3.Java高并发秒杀APi之Web层代码编写。

其实完成这三个模块就可以完成我们的秒杀系统了，但对于我们的秒杀系统中一件秒杀商品，在秒杀的时候肯定会有成千上万的用户参与进来，通过上述三个模块完成的系统无法解决这么多用户的高并发操作，所以我们还需要第四个模块:

4.Java高并发秒杀APi之高并发优化。
