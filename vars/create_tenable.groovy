def call(server) {

    withCredentials([usernamePassword(credentialsId: 'serviceaccount1', passwordVariable: 'secret_password', usernameVariable: 'secret_user')]) {

        def playbookFilePath = _writeWindowsPlaybook(server[0], server[1], server[2])

        sh '''
    export secret_password="$secret_password"
    /usr/local/bin/ansible-playbook "./playbook.yaml" --extra-vars "secret_password=$secret_password"
    '''

    }

}

def _writeWindowsPlaybook(ip, username, password) {

    def playbookContent = """
         
    - name: add ansible script
      hosts: localhost
      gather_facts: no
      tasks:
        - name: Add server
          add_host:
            name: ${ip}
            groups: ade
            ansible_user: ${username}
            ansible_password: ${password}
            ansible_connection: winrm
            ansible_winrm_scheme: http
            ansible_port: 5985
            ansible_winrm_server_cert_validation: ignore
    - name: add ansible script
      hosts: ade
      gather_facts: no
      tasks:
        - meta: clear_host_errors  
        - name: Reset User
          block:
          - win_user:
              name: svc_tenable
              state: absent
            register: output 
            ignore_errors: True
            ignore_unreachable: true
          rescue:
          - debug:
              msg: "{{ output }}"
              ignore_unreachable: true
              ignore_errors: True
          - set_fact:
              task_status: "ERROR"
        
        - name: Add User
          block:
          -  win_user:
               name: svc_tenable
               password: "{{ lookup('env', 'secret_password') }}"
               groups: Administrators
               password_never_expires: yes
               state: present
             register: output
             ignore_errors: True 
             ignore_unreachable: true
      
          - name: saving result.txt for successfull
            shell: echo "${ip} {{ output.state }}" >> result.txt
            delegate_to: localhost 
                 
          rescue:
          - debug:
              msg: "{{ output }}"
          - set_fact:
              task_status: "ERROR"
          - name: Touch the same file, but add/remove some permissions
            file:
              path: result.txt
              state: touch
              mode: u+rw,g+wx,o+rwx
            delegate_to: localhost
          - name: saving result.txt 
            shell: echo "${ip} unable to create user error" >> result.txt
            delegate_to: localhost
         
    """

    writeFile file: './playbook.yaml', text: playbookContent

    return './playbook.yaml'

}