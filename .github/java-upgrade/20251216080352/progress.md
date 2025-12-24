# Upgrade Progress

  ### ✅ Generate Upgrade Plan
  - [[View Log]](logs\1.generatePlan.log)

  ### ✅ Confirm Upgrade Plan
  - [[View Log]](logs\2.confirmPlan.log)

  ### ❗ Setup Development Environment
  - [[View Log]](logs\3.setupEnvironment.log)
  
  <details>
      <summary>[ click to toggle details ]</summary>
  
  #### Errors
  - Project compile failed with 3 errors. The project must be compileable before upgrading it, please fix the errors first and then invoke tool #setup\_upgrade\_environment again to setup development environment: - === Config File error     The below errors can be due to missing dependencies. You may have to refer     to the config files provided earlier to solve it.     'errorMessage': Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile (default-compile) on project xiangqi-shared: Compilation failure: Compilation failure:   /C:/Users/dolphin chan/Desktop/JAVA\\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[233,46] �Ҳ�������     ����:   ���� FINISHED     λ��: �� com.xiangqi.shared.model.GameStatus   /C:/Users/dolphin chan/Desktop/JAVA\\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[234,83] �Ҳ�������     ����:   ���� ResultType     λ��: �� com.xiangqi.shared.model.GameResult   /C:/Users/dolphin chan/Desktop/JAVA\\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[237,46] �Ҳ�������     ����:   ���� FINISHED     λ��: �� com.xiangqi.shared.model.GameStatus   /C:/Users/dolphin chan/Desktop/JAVA\\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[238,70] �Ҳ�������     ����:   ���� ResultType     λ��: �� com.xiangqi.shared.model.GameResult   /C:/Users/dolphin chan/Desktop/JAVA\\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[243,46] �Ҳ�������     ����:   ���� FINISHED     λ��: �� com.xiangqi.shared.model.GameStatus   /C:/Users/dolphin chan/Desktop/JAVA\\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[244,89] �Ҳ�������     ����:   ���� ResultType     λ��: �� com.xiangqi.shared.model.GameResult    \`\`\`   Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile (default-compile) on project xiangqi-shared: Compilation failure: Compilation failure:   /C:/Users/dolphin chan/Desktop/JAVA\\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[233,46] �Ҳ�������     ����:   ���� FINISHED     λ��: �� com.xiangqi.shared.model.GameStatus   /C:/Users/dolphin chan/Desktop/JAVA\\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[234,83] �Ҳ�������     ����:   ���� ResultType     λ��: �� com.xiangqi.shared.model.GameResult   /C:/Users/dolphin chan/Desktop/JAVA\\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[237,46] �Ҳ�������     ����:   ���� FINISHED     λ��: �� com.xiangqi.shared.model.GameStatus   /C:/Users/dolphin chan/Desktop/JAVA\\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[238,70] �Ҳ�������     ����:   ���� ResultType     λ��: �� com.xiangqi.shared.model.GameResult   /C:/Users/dolphin chan/Desktop/JAVA\\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[243,46] �Ҳ�������     ����:   ���� FINISHED     λ��: �� com.xiangqi.shared.model.GameStatus   /C:/Users/dolphin chan/Desktop/JAVA\\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[244,89] �Ҳ�������     ����:   ���� ResultType     λ��: �� com.xiangqi.shared.model.GameResult   \`\`\` - === Config File error     The below errors can be due to missing dependencies. You may have to refer     to the config files provided earlier to solve it.     'errorMessage': Failed to execute goal on project xiangqi-client: Could not resolve dependencies for project com.xiangqi:xiangqi-client:jar:1.0.0   dependency: com.xiangqi:xiangqi-shared:jar:1.0.0 (compile)   	Could not find artifact com.xiangqi:xiangqi-shared:jar:1.0.0 in central (https://repo.maven.apache.org/maven2)    \`\`\`   Failed to execute goal on project xiangqi-client: Could not resolve dependencies for project com.xiangqi:xiangqi-client:jar:1.0.0   dependency: com.xiangqi:xiangqi-shared:jar:1.0.0 (compile)   	Could not find artifact com.xiangqi:xiangqi-shared:jar:1.0.0 in central (https://repo.maven.apache.org/maven2)   \`\`\` - === Config File error     The below errors can be due to missing dependencies. You may have to refer     to the config files provided earlier to solve it.     'errorMessage': Failed to execute goal on project xiangqi-server: Could not resolve dependencies for project com.xiangqi:xiangqi-server:jar:1.0.0   dependency: com.xiangqi:xiangqi-shared:jar:1.0.0 (compile)   	com.xiangqi:xiangqi-shared:jar:1.0.0 was not found in https://repo.maven.apache.org/maven2 during a previous attempt. This failure was cached in the local repository and resolution is not reattempted until the update interval of central has elapsed or updates are forced    \`\`\`   Failed to execute goal on project xiangqi-server: Could not resolve dependencies for project com.xiangqi:xiangqi-server:jar:1.0.0   dependency: com.xiangqi:xiangqi-shared:jar:1.0.0 (compile)   	com.xiangqi:xiangqi-shared:jar:1.0.0 was not found in https://repo.maven.apache.org/maven2 during a previous attempt. This failure was cached in the local repository and resolution is not reattempted until the update interval of central has elapsed or updates are forced   \`\`\`
  </details>

  ### ✅ PreCheck
  - [[View Log]](logs\4.precheck.log)
  
  <details>
      <summary>[ click to toggle details ]</summary>
  
  - ###
    ### ❗ Precheck - Build project
    - [[View Log]](logs\4.1.precheck-buildProject.log)
    
    <details>
        <summary>[ click to toggle details ]</summary>
    
    #### Command
    `mvn clean test-compile -q -B -fn`
    
    #### Errors
    - === Config File error     The below errors can be due to missing dependencies. You may have to refer     to the config files provided earlier to solve it.     'errorMessage': Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile (default-compile) on project xiangqi-shared: Compilation failure: Compilation failure:   /C:/Users/dolphin chan/Desktop/JAVA\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[233,46] �Ҳ�������     ����:   ���� FINISHED     λ��: �� com.xiangqi.shared.model.GameStatus   /C:/Users/dolphin chan/Desktop/JAVA\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[234,83] �Ҳ�������     ����:   ���� ResultType     λ��: �� com.xiangqi.shared.model.GameResult   /C:/Users/dolphin chan/Desktop/JAVA\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[237,46] �Ҳ�������     ����:   ���� FINISHED     λ��: �� com.xiangqi.shared.model.GameStatus   /C:/Users/dolphin chan/Desktop/JAVA\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[238,70] �Ҳ�������     ����:   ���� ResultType     λ��: �� com.xiangqi.shared.model.GameResult   /C:/Users/dolphin chan/Desktop/JAVA\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[243,46] �Ҳ�������     ����:   ���� FINISHED     λ��: �� com.xiangqi.shared.model.GameStatus   /C:/Users/dolphin chan/Desktop/JAVA\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[244,89] �Ҳ�������     ����:   ���� ResultType     λ��: �� com.xiangqi.shared.model.GameResult 
      ```
      Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile (default-compile) on project xiangqi-shared: Compilation failure: Compilation failure:   /C:/Users/dolphin chan/Desktop/JAVA\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[233,46] �Ҳ�������     ����:   ���� FINISHED     λ��: �� com.xiangqi.shared.model.GameStatus   /C:/Users/dolphin chan/Desktop/JAVA\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[234,83] �Ҳ�������     ����:   ���� ResultType     λ��: �� com.xiangqi.shared.model.GameResult   /C:/Users/dolphin chan/Desktop/JAVA\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[237,46] �Ҳ�������     ����:   ���� FINISHED     λ��: �� com.xiangqi.shared.model.GameStatus   /C:/Users/dolphin chan/Desktop/JAVA\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[238,70] �Ҳ�������     ����:   ���� ResultType     λ��: �� com.xiangqi.shared.model.GameResult   /C:/Users/dolphin chan/Desktop/JAVA\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[243,46] �Ҳ�������     ����:   ���� FINISHED     λ��: �� com.xiangqi.shared.model.GameStatus   /C:/Users/dolphin chan/Desktop/JAVA\_ChessProject/xiangqi-shared/src/main/java/com/xiangqi/shared/engine/ChessEngine.java:[244,89] �Ҳ�������     ����:   ���� ResultType     λ��: �� com.xiangqi.shared.model.GameResult
      ```
    - === Config File error     The below errors can be due to missing dependencies. You may have to refer     to the config files provided earlier to solve it.     'errorMessage': Failed to execute goal on project xiangqi-client: Could not resolve dependencies for project com.xiangqi:xiangqi-client:jar:1.0.0   dependency: com.xiangqi:xiangqi-shared:jar:1.0.0 (compile)   	Could not find artifact com.xiangqi:xiangqi-shared:jar:1.0.0 in central (https://repo.maven.apache.org/maven2) 
      ```
      Failed to execute goal on project xiangqi-client: Could not resolve dependencies for project com.xiangqi:xiangqi-client:jar:1.0.0   dependency: com.xiangqi:xiangqi-shared:jar:1.0.0 (compile)   	Could not find artifact com.xiangqi:xiangqi-shared:jar:1.0.0 in central (https://repo.maven.apache.org/maven2)
      ```
    - === Config File error     The below errors can be due to missing dependencies. You may have to refer     to the config files provided earlier to solve it.     'errorMessage': Failed to execute goal on project xiangqi-server: Could not resolve dependencies for project com.xiangqi:xiangqi-server:jar:1.0.0   dependency: com.xiangqi:xiangqi-shared:jar:1.0.0 (compile)   	com.xiangqi:xiangqi-shared:jar:1.0.0 was not found in https://repo.maven.apache.org/maven2 during a previous attempt. This failure was cached in the local repository and resolution is not reattempted until the update interval of central has elapsed or updates are forced 
      ```
      Failed to execute goal on project xiangqi-server: Could not resolve dependencies for project com.xiangqi:xiangqi-server:jar:1.0.0   dependency: com.xiangqi:xiangqi-shared:jar:1.0.0 (compile)   	com.xiangqi:xiangqi-shared:jar:1.0.0 was not found in https://repo.maven.apache.org/maven2 during a previous attempt. This failure was cached in the local repository and resolution is not reattempted until the update interval of central has elapsed or updates are forced
      ```
    </details>
  </details>

  ### ❗ Setup Development Environment
  - [[View Log]](logs\5.setupEnvironment.log)
  
  <details>
      <summary>[ click to toggle details ]</summary>
  
  #### Errors
  - Canceled
  </details>

  ### ✅ PreCheck
  - [[View Log]](logs\6.precheck.log)
  
  <details>
      <summary>[ click to toggle details ]</summary>
  
  - ###
    ### ✅ Precheck - Build project
    - [[View Log]](logs\6.1.precheck-buildProject.log)
    
    <details>
        <summary>[ click to toggle details ]</summary>
    
    #### Command
    `mvn clean test-compile -q -B -fn`
    </details>
  
    ### ✅ Precheck - Validate CVEs
    - [[View Log]](logs\6.2.precheck-validateCves.log)
    
    <details>
        <summary>[ click to toggle details ]</summary>
    
    #### CVE issues
    </details>
  
    ### ✅ Precheck - Run tests
    - [[View Log]](logs\6.3.precheck-runTests.log)
    
    <details>
        <summary>[ click to toggle details ]</summary>
    
    #### Test result
    | Total | Passed | Failed | Skipped | Errors |
    |-------|--------|--------|---------|--------|
    | 0 | 0 | 0 | 0 | 0 |
    </details>
  </details>