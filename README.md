## springboot+beetlsql+gradle架构

## 1、项目检出

  git clone https://github.com/zqYang789/springboot.git

## 2、数据源配置

  修改配置文件：src->resource->application-dev.yml
  
## 3、跨域处理

  修改文件：src->com->sunsan->framework->filter->ApiOriginFilter,在allowDomain数组中加入需要允许访问的前端地址
