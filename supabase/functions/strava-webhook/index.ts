import { serve } from "https://deno.land/std@0.168.0/http/server.ts"

serve(async (req) => {
    const url = new URL(req.url)

    // 1. Webhook Validation (GET)
    // Strava sends a GET request to verify the endpoint when you create the webhook subscription
    if (req.method === 'GET') {
        const mode = url.searchParams.get("hub.mode")
        const token = url.searchParams.get("hub.verify_token")
        const challenge = url.searchParams.get("hub.challenge")

        // Read your custom verify token from Supabase secrets
        const VERIFY_TOKEN = Deno.env.get('STRAVA_VERIFY_TOKEN')

        if (mode === 'subscribe' && token === VERIFY_TOKEN) {
            console.log('WEBHOOK_VERIFIED')
            return new Response(JSON.stringify({ "hub.challenge": challenge }), {
                status: 200,
                headers: { "Content-Type": "application/json" }
            })
        } else {
            return new Response("Forbidden", { status: 403 })
        }
    }

    // 2. Webhook Event push (POST)
    // Strava pushes updates here when activities are created/updated/deleted
    if (req.method === 'POST') {
        try {
            const body = await req.json()
            console.log("Strava webhook event received:", body)

            const { object_type, aspect_type, object_id, owner_id } = body

            if (object_type === 'activity') {
                // You could initialize the Supabase client here and insert/update
                // the activity directly into your Postgres database.

                // Example logic (pseudocode):
                // const supabase = createClient(supabaseUrl, supabaseServiceRoleKey)
                // 
                // if (aspect_type === 'create' || aspect_type === 'update') {
                //    const tokens = await getTokensForUser(owner_id)
                //    const activityDetail = await fetchFromStrava(object_id, tokens)
                //    await supabase.from('activities').upsert(activityDetail)
                // } else if (aspect_type === 'delete') {
                //    await supabase.from('activities').delete().eq('id', object_id)
                // }
            }

            // Always respond with 200 OK to acknowledge receipt, otherwise Strava retries
            return new Response("EVENT_RECEIVED", { status: 200 })

        } catch (e) {
            console.error(e)
            // Even on error, we send 200 back so Strava stops retrying bad payloads
            return new Response("Error processing event", { status: 200 })
        }
    }

    return new Response("Method not allowed", { status: 405 })
})
