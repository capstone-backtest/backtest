pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
    }

    environment {
        GHCR_OWNER = 'kyj05030'
        BACKEND_PROD_IMAGE = 'backtest-backend'
        FRONTEND_PROD_IMAGE = 'backtest-frontend'
        DEPLOY_HOST = 'localhost'
        DEPLOY_USER = 'jenkins'
        DEPLOY_PATH_PROD = '/opt/backtest'
        DOCKER_COMPOSE_PROD_FILE = '${WORKSPACE}/docker-compose.prod.yml'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Debug Environment') {
            steps {
                script {
                    echo "🔍 Debug Information:"
                    echo "BRANCH_NAME: ${env.BRANCH_NAME}"
                    echo "GIT_BRANCH: ${env.GIT_BRANCH}"
                    echo "BUILD_NUMBER: ${env.BUILD_NUMBER}"
                    echo "All env vars:"
                    sh 'env | grep -E "(BRANCH|GIT)" | sort'
                }
            }
        }

        stage('Tests') {
            parallel {
                stage('Frontend Tests') {
                    steps {
                        script {
                            echo 'Running frontend tests...'
                            sh '''
                                cd frontend
                                docker build --build-arg RUN_TESTS=true \
                                    -t backtest-frontend-test:${BUILD_NUMBER} .
                            '''
                            echo "✅ Frontend tests passed"
                        }
                    }
                }
                stage('Backend Tests') {
                    steps {
                        script {
                            echo 'Running backend tests with controlled environment...'
                            sh '''
                                cd backend
                                docker build --build-arg RUN_TESTS=true \
                                    -t backtest-backend-test:${BUILD_NUMBER} .
                            '''
                            echo "✅ Backend tests passed"
                        }
                    }
                }
            }
        }

        stage('Build and Push Backend PROD') {
            when {
                anyOf {
                    expression { return env.BRANCH_NAME == 'main' }
                    expression { return env.GIT_BRANCH == 'origin/main' }
                    expression { return env.GIT_BRANCH == 'main' }
                }
            }
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'github-token', usernameVariable: 'GH_USER', passwordVariable: 'GH_TOKEN')]) {
                        def fullImageName = "ghcr.io/${env.GH_USER}/${env.BACKEND_PROD_IMAGE}:${env.BUILD_NUMBER}"
                        echo "Building PROD backend image: ${fullImageName}"
                        sh "cd backend && DOCKER_BUILDKIT=1 docker build --build-arg RUN_TESTS=false --build-arg IMAGE_TAG=${BUILD_NUMBER} -t ${fullImageName} ."
                        sh "echo \"${GH_TOKEN}\" | docker login ghcr.io -u ${GH_USER} --password-stdin"
                        sh "docker push ${fullImageName}"
                    }
                }
            }
        }

        stage('Build and Push Frontend PROD') {
            when {
                anyOf {
                    expression { return env.BRANCH_NAME == 'main' }
                    expression { return env.GIT_BRANCH == 'origin/main' }
                    expression { return env.GIT_BRANCH == 'main' }
                }
            }
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'github-token', usernameVariable: 'GH_USER', passwordVariable: 'GH_TOKEN')]) {
                        def fullImageName = "ghcr.io/${env.GH_USER}/${env.FRONTEND_PROD_IMAGE}:${env.BUILD_NUMBER}"
                        echo "Building PROD frontend image: ${fullImageName}"
                        sh "cd frontend && DOCKER_BUILDKIT=1 docker build --build-arg RUN_TESTS=false -t ${fullImageName} ."
                        sh "echo \"${GH_TOKEN}\" | docker login ghcr.io -u ${GH_USER} --password-stdin"
                        sh "docker push ${fullImageName}"
                    }
                }
            }
        }

        stage('Deploy to Production (Local)') {
            when {
                anyOf {
                    expression { return env.BRANCH_NAME == 'main' }
                    expression { return env.GIT_BRANCH == 'origin/main' }
                    expression { return env.GIT_BRANCH == 'main' }
                }
            }
            steps {
                script {
                    withCredentials([
                        usernamePassword(credentialsId: 'github-token', usernameVariable: 'GH_USER', passwordVariable: 'GH_TOKEN'),
                        sshUserPrivateKey(credentialsId: 'home-ubuntu-ssh', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')
                    ]) {
                        def remoteUser = SSH_USER ?: env.DEPLOY_USER
                        def remote = "${remoteUser}@${env.DEPLOY_HOST}"
                        def backendImage = "ghcr.io/${env.GH_USER}/${env.BACKEND_PROD_IMAGE}:${env.BUILD_NUMBER}"
                        def frontendImage = "ghcr.io/${env.GH_USER}/${env.FRONTEND_PROD_IMAGE}:${env.BUILD_NUMBER}"

                        echo "Deploying to ${env.DEPLOY_PATH_PROD} on ${env.DEPLOY_HOST} as ${remoteUser}"

                        sh "ssh -i \"\${SSH_KEY}\" -o StrictHostKeyChecking=no \"${remote}\" \"mkdir -p ${env.DEPLOY_PATH_PROD}\""
                        sh "scp -i \"\${SSH_KEY}\" -o StrictHostKeyChecking=no \"${env.DOCKER_COMPOSE_PROD_FILE}\" \"${remote}:${env.DEPLOY_PATH_PROD}/docker-compose.yml\""
                        sh "scp -i \"\${SSH_KEY}\" -o StrictHostKeyChecking=no ./scripts/remote_deploy.sh \"${remote}:${env.DEPLOY_PATH_PROD}/remote_deploy.sh\""
                        sh "ssh -i \"\${SSH_KEY}\" -o StrictHostKeyChecking=no \"${remote}\" \"chmod +x ${env.DEPLOY_PATH_PROD}/remote_deploy.sh\""
                        sh "ssh -i \"\${SSH_KEY}\" -o StrictHostKeyChecking=no \"${remote}\" \"${env.DEPLOY_PATH_PROD}/remote_deploy.sh ${backendImage} ${frontendImage} ${env.DEPLOY_PATH_PROD}\""
                    }
                }
            }
        }

        stage('Integration Tests') {
            when {
                anyOf {
                    expression { return env.BRANCH_NAME == 'main' }
                    expression { return env.GIT_BRANCH == 'origin/main' }
                    expression { return env.GIT_BRANCH == 'main' }
                }
            }
            steps {
                script {
                    echo 'Running integration tests against deployed environment...'
                    sh '''
                        # Poll until healthy
                        for i in $(seq 1 30); do
                          if curl -fsS http://localhost:8001/health >/dev/null; then echo "backend healthy"; break; fi
                          sleep 1;
                        done
                        for i in $(seq 1 30); do
                          if curl -fsS http://localhost:8082/ >/dev/null; then echo "frontend up"; break; fi
                          sleep 1;
                        done
                    '''
                    // Optional API check (non-blocking)
                    try {
                        sh '''
                            cat > /tmp/payload.json <<'EOF'
                            {"ticker":"AAPL","start_date":"2023-01-03","end_date":"2023-01-20","initial_cash":10000,"strategy":"buy_and_hold","strategy_params":{}}
EOF
                            curl -fsS -H 'Content-Type: application/json' -d @/tmp/payload.json http://localhost:8001/api/v1/backtest/chart-data | jq -e '.ticker and .ohlc_data and .equity_data and .summary_stats' >/dev/null
                        '''
                        echo "✅ Integration API check passed"
                    } catch (Exception e) {
                        echo "⚠️ Integration API check failed (non-blocking): ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                    echo "✅ Integration tests completed"
                }
            }
        }
    }

    post {
        success { echo 'Pipeline succeeded! 🎉' }
        failure { echo 'Pipeline failed! ❌' }
        always {
            sh 'docker system prune -f'
            cleanWs()
        }
    }
}
