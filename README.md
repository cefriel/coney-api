# Coney Api

Coney API: backend of the Coney toolkit.

## Customize build configuration

The following parameter can be modified for a specific build:
- Log directory path: in file `src\main\resources\log4j.xml` modify the attribute `value` for the element `param` with attribute `name` equal to FileNamePattern.
- Unpublished surveys directory path: in class `src\main\java\com\cefriel\coneyapi\utils\Utils.java` in the `saveJsonToFile` method modify the variable `absolutePath`. 
- Swagger documentation enable/disable: in class `src\main\java\com\cefriel\coneyapi\config\ApplicationConfig.java` modify the boolean in the `api` method when calling `enable`.

`TODO` Make these variables configurable withouth modifying the code.