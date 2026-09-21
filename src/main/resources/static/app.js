const API = ""; // same host (Spring serves / plus endpoints like /products)

// ---------- helpers ----------
const qs = (s) => document.querySelector(s);

function formatMoneyFromCents(cents) {
  const value = (cents || 0) / 100;
  return value.toLocaleString("en-US", { style: "currency", currency: "BRL" });
}

function setMsg(el, text) {
  el.textContent = text || "";
}

async function apiGet(path) {
  const res = await fetch(API + path);
  const data = await res.json().catch(() => null);
  if (!res.ok) throw data || { message: "Unexpected error" };
  return data;
}

async function apiPost(path, body) {
  const res = await fetch(API + path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) throw data || { message: "Unexpected error" };
  return data;
}

// ---------- Tabs (persisted across reloads) ----------
const TAB_KEY = "mm_tab_v1";

function activateTab(name) {
  document.querySelectorAll(".tab").forEach((b) => b.classList.toggle("active", b.dataset.tab === name));
  document.querySelectorAll(".tab-panel").forEach((p) => p.classList.toggle("active", p.id === `tab-${name}`));
  try {
    localStorage.setItem(TAB_KEY, name);
  } catch {
    // private mode etc. — tabs just won't persist
  }
}

document.querySelectorAll(".tab").forEach((btn) => {
  btn.addEventListener("click", () => activateTab(btn.dataset.tab));
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

function cartCount(cart) {
  return cart.reduce((sum, i) => sum + i.quantity, 0);
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
  filterCategoryEl.innerHTML = `<option value="">All categories</option>` + cats.map(c => `<option value="${c}">${c}</option>`).join("");
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

  if (filtered.length === 0) {
    productsListEl.innerHTML = `<p class="muted">No products match these filters.</p>`;
    return;
  }

  productsListEl.innerHTML = filtered.map((p) => `
    <div class="row">
      <div class="thumb" aria-hidden="true">${(p.name || "?").charAt(0).toUpperCase()}</div>
      <div>
        <h4>${p.name} <span class="pill">${p.category}</span></h4>
        <div class="muted">${formatMoneyFromCents(p.priceCents)} • ${p.active ? "Active" : "Inactive"}</div>
      </div>
      <div class="right">
        <button class="btn" ${p.active ? "" : "disabled"} data-add="${p.id}">Add</button>
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

  if (!categoriesInitialized) {
    const initData = await apiGet("/products");
    fillCategories(initData);
    categoriesInitialized = true;
  }

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
const cartCountEl = qs("#cartCount");
const checkoutMsgEl = qs("#checkoutMsg");

function renderCart() {
  const cart = loadCart();
  const total = cartTotalCents(cart);

  cartTotalEl.textContent = formatMoneyFromCents(total);
  cartCountEl.textContent = `${cartCount(cart)} item${cartCount(cart) === 1 ? "" : "s"}`;

  if (cart.length === 0) {
    cartListEl.innerHTML = `<p class="muted">Cart is empty.</p>`;
    return;
  }

  cartListEl.innerHTML = cart.map((i) => `
    <div class="row">
      <div>
        <h4>${i.name}</h4>
        <div class="muted">${formatMoneyFromCents(i.priceCents)} • Subtotal: <strong>${formatMoneyFromCents(i.priceCents * i.quantity)}</strong></div>
      </div>
      <div class="right">
        <div class="qty">
          <span class="muted">Qty</span>
          <input type="number" min="0" value="${i.quantity}" data-qty="${i.productId}" aria-label="Quantity for ${i.name}" />
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
    setMsg(checkoutMsgEl, "Cart is empty.");
    return;
  }

  const customerId = Number(qs("#customerId").value);
  if (!customerId || customerId <= 0) {
    setMsg(checkoutMsgEl, "Invalid customer ID.");
    return;
  }

  const body = {
    customerId,
    items: cart.map((i) => ({ productId: i.productId, quantity: i.quantity })),
  };

  try {
    const res = await apiPost("/orders", body);
    setMsg(checkoutMsgEl, `Order placed! #${res.orderId} • Total: ${formatMoneyFromCents(res.totalCents)}`);
    clearCart();
    await loadOrders();
  } catch (err) {
    setMsg(checkoutMsgEl, err.message || "Failed to create order.");
  }
});

// ---------- Orders ----------
const ordersListEl = qs("#ordersList");
const orderDetailEl = qs("#orderDetail");

async function loadOrders() {
  const orders = await apiGet("/orders");

  if (orders.length === 0) {
    ordersListEl.innerHTML = `<p class="muted">No orders yet — place one in the Shop tab.</p>`;
    return;
  }

  ordersListEl.innerHTML = orders.map((o) => `
    <div class="row">
      <div>
        <h4>Order #${o.id} <span class="pill status-${String(o.status).toLowerCase()}">${o.status}</span></h4>
        <div class="muted">${o.createdAt} • Total: <strong>${formatMoneyFromCents(o.totalCents)}</strong></div>
      </div>
      <div class="right">
        <button class="btn" data-order="${o.id}">View</button>
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
        <h4>Order #${o.id} <span class="pill status-${String(o.status).toLowerCase()}">${o.status}</span></h4>
        <div class="muted">Customer: ${o.customerId} • ${o.createdAt}</div>
        <div class="muted">Total: <strong>${formatMoneyFromCents(o.totalCents)}</strong></div>
      </div>
    </div>

    <h3 style="margin-top: 12px;">Items</h3>
    <div class="list">
      ${o.items.map(i => `
        <div class="row">
          <div>
            <h4>Product ${i.productId}</h4>
            <div class="muted">Qty: ${i.quantity} • Unit: ${formatMoneyFromCents(i.unitPriceCents)}</div>
          </div>
          <div class="right">
            <div><strong>${formatMoneyFromCents(i.subtotalCents)}</strong></div>
          </div>
        </div>
      `).join("")}
    </div>
  `;
}

qs("#btnReloadOrders").addEventListener("click", loadOrders);

// ---------- init (resilient: cold starts can be slow, so retry instead of going blank) ----------
async function withRetry(fn, attempts = 3) {
  let lastErr;
  for (let i = 0; i < attempts; i++) {
    try {
      return await fn();
    } catch (err) {
      lastErr = err;
      await new Promise((r) => setTimeout(r, 700 * (i + 1)));
    }
  }
  throw lastErr;
}

function bindRetryButtons() {
  document.querySelectorAll("[data-retry]").forEach((b) =>
    b.addEventListener("click", () => void initLoad())
  );
}

async function initLoad() {
  productsListEl.innerHTML = `<p class="muted">Loading products…</p>`;
  ordersListEl.innerHTML = `<p class="muted">Loading orders…</p>`;
  const [products, orders] = await Promise.allSettled([
    withRetry(loadProducts),
    withRetry(loadOrders),
  ]);
  if (products.status === "rejected") {
    productsListEl.innerHTML = `<p class="muted">Could not load products. <button class="btn" type="button" data-retry>Retry</button></p>`;
  }
  if (orders.status === "rejected") {
    ordersListEl.innerHTML = `<p class="muted">Could not load orders. <button class="btn" type="button" data-retry>Retry</button></p>`;
  }
  bindRetryButtons();
}

(function init() {
  let savedTab = "shop";
  try {
    const stored = localStorage.getItem(TAB_KEY);
    if (stored === "shop" || stored === "orders") savedTab = stored;
  } catch {
    // ignore — default to shop
  }
  activateTab(savedTab);
  renderCart();
  void initLoad();
})();
