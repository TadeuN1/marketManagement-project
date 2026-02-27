const API = ""; // mesmo host (Spring serve / e endpoints /products etc.)

// ---------- helpers ----------
const qs = (s) => document.querySelector(s);

function formatBRLFromCents(cents) {
  const value = (cents || 0) / 100;
  return value.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function setMsg(el, text) {
  el.textContent = text || "";
}

async function apiGet(path) {
  const res = await fetch(API + path);
  const data = await res.json().catch(() => null);
  if (!res.ok) throw data || { message: "Erro inesperado" };
  return data;
}

async function apiPost(path, body) {
  const res = await fetch(API + path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) throw data || { message: "Erro inesperado" };
  return data;
}

// ---------- Tabs ----------
document.querySelectorAll(".tab").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab").forEach((b) => b.classList.remove("active"));
    document.querySelectorAll(".tab-panel").forEach((p) => p.classList.remove("active"));

    btn.classList.add("active");
    qs(`#tab-${btn.dataset.tab}`).classList.add("active");
  });
});

// ---------- Cart (local) ----------
const CART_KEY = "mm_cart_v1";

function loadCart() {
  try {
    return JSON.parse(localStorage.getItem(CART_KEY)) || [];
  } catch {
    return [];
  }
}

function saveCart(items) {
  localStorage.setItem(CART_KEY, JSON.stringify(items));
}

function cartTotalCents(cart) {
  return cart.reduce((sum, i) => sum + i.quantity * i.priceCents, 0);
}

function upsertCartItem(product, quantity) {
  const cart = loadCart();
  const idx = cart.findIndex((i) => i.productId === product.id);

  if (idx >= 0) cart[idx].quantity += quantity;
  else cart.push({ productId: product.id, name: product.name, priceCents: product.priceCents, quantity });

  saveCart(cart);
  renderCart();
}

function setCartItemQty(productId, quantity) {
  const cart = loadCart().map((i) => (i.productId === productId ? { ...i, quantity } : i))
    .filter((i) => i.quantity > 0);
  saveCart(cart);
  renderCart();
}

function clearCart() {
  saveCart([]);
  renderCart();
}

// ---------- Products ----------
const productsListEl = qs("#productsList");
const searchNameEl = qs("#searchName");
const filterCategoryEl = qs("#filterCategory");
const onlyActiveEl = qs("#onlyActive");

let allProducts = [];
let categoriesInitialized = false;

function uniqueCategories(products) {
  return [...new Set(products.map((p) => p.category))].sort();
}

function fillCategories(products) {
  const cats = uniqueCategories(products);
  filterCategoryEl.innerHTML = `<option value="">Todas as categorias</option>` + cats.map(c => `<option value="${c}">${c}</option>`).join("");
}

function applyProductFilters() {
  const name = (searchNameEl.value || "").trim().toLowerCase();
  const cat = filterCategoryEl.value;
  const onlyActive = onlyActiveEl.checked;

  return allProducts.filter((p) => {
    if (onlyActive && !p.active) return false;
    if (cat && p.category !== cat) return false;
    if (name && !p.name.toLowerCase().includes(name)) return false;
    return true;
  });
}

function renderProducts() {
  const filtered = applyProductFilters();

  productsListEl.innerHTML = filtered.map((p) => `
    <div class="row">
      <div>
        <h4>${p.name} <span class="pill">${p.category}</span></h4>
        <div class="muted">${formatBRLFromCents(p.priceCents)} • ${p.active ? "Ativo" : "Inativo"}</div>
      </div>
      <div class="right">
        <button class="btn" ${p.active ? "" : "disabled"} data-add="${p.id}">Adicionar</button>
      </div>
    </div>
  `).join("");

  productsListEl.querySelectorAll("[data-add]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const id = Number(btn.dataset.add);
      const product = allProducts.find((x) => x.id === id);
      upsertCartItem(product, 1);
    });
  });
}

async function loadProducts() {
  const onlyActive = onlyActiveEl.checked;
  const category = filterCategoryEl.value || "";

  // 1) Preenche categorias só uma vez com TODOS os produtos
  if (!categoriesInitialized) {
    const initData = await apiGet("/products"); // ✅ sem active/category
    fillCategories(initData);
    categoriesInitialized = true;
  }

  // 2) Agora carrega com os filtros escolhidos
  const params = new URLSearchParams();
  if (category) params.set("category", category);
  params.set("active", String(onlyActive));

  const data = await apiGet(`/products?${params.toString()}`);
  allProducts = data;

  renderProducts();
}




qs("#btnReloadProducts").addEventListener("click", loadProducts);
searchNameEl.addEventListener("input", () => renderProducts());
filterCategoryEl.addEventListener("change", loadProducts);
onlyActiveEl.addEventListener("change", loadProducts);

// ---------- Cart UI ----------
const cartListEl = qs("#cartList");
const cartTotalEl = qs("#cartTotal");
const checkoutMsgEl = qs("#checkoutMsg");

function renderCart() {
  const cart = loadCart();
  const total = cartTotalCents(cart);

  cartTotalEl.textContent = formatBRLFromCents(total);

  if (cart.length === 0) {
    cartListEl.innerHTML = `<p class="muted">Carrinho vazio.</p>`;
    return;
  }

  cartListEl.innerHTML = cart.map((i) => `
    <div class="row">
      <div>
        <h4>${i.name}</h4>
        <div class="muted">${formatBRLFromCents(i.priceCents)} • Subtotal: <strong>${formatBRLFromCents(i.priceCents * i.quantity)}</strong></div>
      </div>
      <div class="right">
        <div class="qty">
          <span class="muted">Qtd</span>
          <input type="number" min="0" value="${i.quantity}" data-qty="${i.productId}" />
        </div>
      </div>
    </div>
  `).join("");

  cartListEl.querySelectorAll("[data-qty]").forEach((input) => {
    input.addEventListener("change", () => {
      const pid = Number(input.dataset.qty);
      const q = Number(input.value);
      if (Number.isNaN(q) || q < 0) return;
      setCartItemQty(pid, q);
    });
  });
}

// ---------- Checkout (POST /orders) ----------
qs("#btnCheckout").addEventListener("click", async () => {
  setMsg(checkoutMsgEl, "");

  const cart = loadCart();
  if (cart.length === 0) {
    setMsg(checkoutMsgEl, "Carrinho vazio.");
    return;
  }

  const customerId = Number(qs("#customerId").value);
  if (!customerId || customerId <= 0) {
    setMsg(checkoutMsgEl, "Customer ID inválido.");
    return;
  }

  const body = {
    customerId,
    items: cart.map((i) => ({ productId: i.productId, quantity: i.quantity })),
  };

  try {
    const res = await apiPost("/orders", body);
    setMsg(checkoutMsgEl, `Pedido criado! Número: ${res.orderId} • Total: ${formatBRLFromCents(res.totalCents)}`);
    clearCart();
    await loadOrders();
  } catch (err) {
    setMsg(checkoutMsgEl, err.message || "Erro ao criar pedido.");
  }
});

// ---------- Orders ----------
const ordersListEl = qs("#ordersList");
const orderDetailEl = qs("#orderDetail");

async function loadOrders() {
  const orders = await apiGet("/orders");

  ordersListEl.innerHTML = orders.map((o) => `
    <div class="row">
      <div>
        <h4>Pedido #${o.id}<span class="pill">${o.status}</span></h4>
        <div class="muted">${o.createdAt} • Total: <strong>${formatBRLFromCents(o.totalCents)}</strong></div>
      </div>
      <div class="right">
        <button class="btn" data-order="${o.id}">Ver</button>
      </div>
    </div>
  `).join("");

  ordersListEl.querySelectorAll("[data-order]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const id = Number(btn.dataset.order);
      await loadOrderDetail(id);
    });
  });
}

async function loadOrderDetail(id) {
  const o = await apiGet(`/orders/${id}`);

  orderDetailEl.innerHTML = `
    <div class="row">
      <div>
        <h4>Pedido #${o.id} <span class="pill">${o.status}</span></h4>
        <div class="muted">Cliente: ${o.customerId} • ${o.createdAt}</div>
        <div class="muted">Total: <strong>${formatBRLFromCents(o.totalCents)}</strong></div>
      </div>
    </div>

    <h3 style="margin-top: 12px;">Itens</h3>
    <div class="list">
      ${o.items.map(i => `
        <div class="row">
          <div>
            <h4>Produto ${i.productId}</h4>
            <div class="muted">Qtd: ${i.quantity} • Unit: ${formatBRLFromCents(i.unitPriceCents)}</div>
          </div>
          <div class="right">
            <div><strong>${formatBRLFromCents(i.subtotalCents)}</strong></div>
          </div>
        </div>
      `).join("")}
    </div>
  `;
}

qs("#btnReloadOrders").addEventListener("click", loadOrders);

// ---------- init ----------
renderCart();
loadProducts().catch(() => {});
loadOrders().catch(() => {});
