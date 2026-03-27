#!/bin/bash

# Maven Zscaler Proxy Configuration Script
# Run this script to configure Maven for Zscaler authentication

echo "========== MAVEN ZSCALER PROXY SETUP =========="
echo ""

# Variables - REPLACE THESE WITH YOUR ACTUAL VALUES
PROXY_HOST="${1:-proxy.cgi.com}"
PROXY_PORT="${2:-8080}"
PROXY_USER="${3:-Boopathi.S.Subramanian@cgi.com}"
PROXY_PASSWORD="${4:-YOUR_PASSWORD}"

echo "Proxy Configuration:"
echo "  Host: $PROXY_HOST"
echo "  Port: $PROXY_PORT"
echo "  Username: $PROXY_USER"
echo ""

# Create Maven settings.xml if it doesn't exist
SETTINGS_FILE="$HOME/.m2/settings.xml"
mkdir -p "$HOME/.m2"

# Backup existing settings if present
if [ -f "$SETTINGS_FILE" ]; then
    cp "$SETTINGS_FILE" "$SETTINGS_FILE.bak"
    echo "✓ Backed up existing settings to $SETTINGS_FILE.bak"
fi

# Create settings.xml with proxy configuration
cat > "$SETTINGS_FILE" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <!-- Proxy Configuration for Zscaler -->
  <proxies>
    <proxy>
      <id>zscaler-proxy</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>$PROXY_HOST</host>
      <port>$PROXY_PORT</port>
      <username>$PROXY_USER</username>
      <password>$PROXY_PASSWORD</password>
      <nonProxyHosts>localhost|127.0.0.1|*.local|*.home</nonProxyHosts>
    </proxy>
    <proxy>
      <id>zscaler-proxy-https</id>
      <active>true</active>
      <protocol>https</protocol>
      <host>$PROXY_HOST</host>
      <port>$PROXY_PORT</port>
      <username>$PROXY_USER</username>
      <password>$PROXY_PASSWORD</password>
      <nonProxyHosts>localhost|127.0.0.1|*.local|*.home</nonProxyHosts>
    </proxy>
  </proxies>

  <!-- Mirror Configuration (Optional - faster downloads) -->
  <mirrors>
    <mirror>
      <id>maven-default-http-blocker</id>
      <mirrorOf>external:http:*</mirrorOf>
      <name>Pseudo repository to mirror external repositories initially over HTTP.</name>
      <url>https://repo.maven.apache.org/maven2</url>
      <blocked>false</blocked>
    </mirror>
  </mirrors>

</settings>
EOF

echo "✓ Created Maven settings.xml at: $SETTINGS_FILE"
echo ""
echo "========== NEXT STEPS =========="
echo ""
echo "1. Edit $SETTINGS_FILE and replace these values:"
echo "   - <host>$PROXY_HOST</host>"
echo "   - <port>$PROXY_PORT</port>"
echo "   - <username>$PROXY_USER</username>"
echo "   - <password>YOUR_PASSWORD</password>"
echo ""
echo "2. Test Maven build:"
echo "   /opt/homebrew/bin/mvn clean compile"
echo ""
echo "3. If still failing, try disabling SSL verification:"
echo "   /opt/homebrew/bin/mvn clean install -DskipTests \\"
echo "     -Dmaven.wagon.http.ssl.insecure=true \\"
echo "     -Dmaven.wagon.http.ssl.allowall=true"
echo ""
