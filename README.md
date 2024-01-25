# code-generator

Code generator.

This generator can generate a complete set of Entity, DTO, VO, Mapper, Service, and Controller.

Executable CRUD code, this set of code can standardize Java interface construction.

If there is a new interface added, its function cannot be duplicated.

***

### generator.properties说明

- mainPath生成项目的主包名
- package代码包路径
- moduleName功能模块名
- pathPrefix生成URL前缀
- author代码生成人
- email邮件
- tablePrefix表前缀(类名不会包含表前缀)

### application.yml
- 数据库配置

### 启动项目
访问localhost:8080



***

代码生成器。
此生成器能够生成Entity,DTO,VO,Mapper,Service,Controller.
一整套可执行的CRUD代码，此套代码可以规范Java接口建设。
如有新加的接口，则其功能不能重复