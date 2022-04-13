# go-kratos-helper
go kratos 助手工具

## 待办
- [x] 根据protobuf文件生成service文件，支持methods合并 （ps meassage名称修改会重复methods）
- [ ] service 自动wire注入
- [ ] domain object 生成proto message
- [ ] domain object 生成ent gorm 结构
- [ ] domain object 生成dto（proto）、gorm、ent的struct转换方法
- [ ] struct对象提取interface
- [ ] biz/service 工厂方法快速创建，参数自动注入
- [ ] domain value object 快速创建

## 考虑的目标
- [ ] 创建项目中增加 kratos 脚手架
- [ ] grpc http server 注入与路由注册
- [ ] domain object 工厂
- [ ] proto 一键生成 ent/grom 增删查改
- [ ] openapi 快速同步yapi/apifox
- [ ] sql ddl 生成 domain object