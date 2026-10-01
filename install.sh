#!/bin/bash
set -e

# YU-Kami 企业级卡密系统 - Linux 一键安装脚本
# 支持: Ubuntu 20.04+, Debian 11+, CentOS 7+

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

REPO_URL="https://github.com/Ms-liyc/YU-Kami.git"
INSTALL_DIR="/opt/yu-kami"

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用 root 权限运行: sudo ./install.sh"
        exit 1
    fi
}

detect_os() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$ID
        VER=$VERSION_ID
    else
        log_error "无法检测操作系统"
        exit 1
    fi
    log_info "检测到系统: $OS $VER"
}

install_docker() {
    if command -v docker &> /dev/null; then
        log_info "Docker 已安装"
        return
    fi
    log_info "安装 Docker..."
    curl -fsSL https://get.docker.com | sh
    systemctl enable docker
    systemctl start docker
}

install_docker_compose() {
    if docker compose version &> /dev/null; then
        log_info "Docker Compose 已安装"
        return
    fi
    log_info "安装 Docker Compose..."
    apt-get update && apt-get install -y docker-compose-plugin 2>/dev/null || \
    yum install -y docker-compose-plugin 2>/dev/null || true
}

clone_or_update() {
    if [ -d "$INSTALL_DIR" ]; then
        log_info "更新已有安装..."
        cd "$INSTALL_DIR"
        git pull origin main
    else
        log_info "克隆项目..."
        git clone "$REPO_URL" "$INSTALL_DIR"
        cd "$INSTALL_DIR"
    fi
}

start_services() {
    log_info "构建并启动服务（首次可能需要几分钟）..."
    docker compose down 2>/dev/null || true
    docker compose up -d --build
}

wait_for_backend() {
    log_info "等待后端启动..."
    for i in $(seq 1 30); do
        if curl -sf http://localhost:8080/api/shop/products?page=1&size=1 > /dev/null 2>&1; then
            log_info "后端已就绪"
            return
        fi
        sleep 3
    done
    log_warn "后端启动超时，请检查日志: docker compose logs backend"
}

print_success() {
    SERVER_IP=$(hostname -I | awk '{print $1}')
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  YU-Kami 安装完成！${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "  用户前台:  http://${SERVER_IP}/shop"
    echo "  管理后台:  http://${SERVER_IP}/login"
    echo "  默认账号:  admin / admin123"
    echo ""
    echo -e "  ${YELLOW}支付对接（可选）:${NC}"
    echo "    首次部署默认启用模拟支付，可直接体验购买流程。"
    echo "    如需支付宝/微信收款，请登录管理后台 → 订单管理 → 支付配置"
    echo "    并按你的域名设置 PAYMENT_BASE_URL 等环境变量，详见 docs/PAYMENT.md"
    echo ""
    echo "  常用命令:"
    echo "    cd $INSTALL_DIR"
    echo "    docker compose logs -f        # 查看日志"
    echo "    docker compose restart        # 重启服务"
    echo "    docker compose down           # 停止服务"
    echo ""
}

main() {
    echo ""
    echo "  YU-Kami 企业级卡密系统 - 一键安装"
    echo "  屿宸科技 https://github.com/Ms-liyc/YU-Kami"
    echo ""
    check_root
    detect_os
    install_docker
    install_docker_compose
    clone_or_update
    start_services
    wait_for_backend
    print_success
}

main "$@"
