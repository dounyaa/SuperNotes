# Définit la box de base
Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/bionic64"

  # Configuration du provisionnement
  config.vm.provision "shell", inline: <<-SHELL
    # Mise à jour des paquets
    sudo apt-get update

    # Installation de Java 21
    sudo apt-add-repository ppa:openjdk-r/ppa
    sudo apt-get update
    sudo apt-get install -y openjdk-21-jdk

    # Installation de SQLite
    sudo apt-get install -y sqlite3

    # Copie des fichiers du projet dans la VM
    mkdir /app
    cp -R /vagrant/* /app/
    cd /app

    # Exécution de Gradle pour construire le projet
    ./gradlew build
  SHELL

  # Définit le port forwarding
  config.vm.network "forwarded_port", guest: 8080, host: 8080
end
